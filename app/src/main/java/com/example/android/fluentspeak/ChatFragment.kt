package com.example.android.fluentspeak

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.android.fluentspeak.database.ConversationWithUtterances
import com.example.android.fluentspeak.database.Utterance
import com.example.android.fluentspeak.databinding.FragmentChatBinding
import com.example.android.fluentspeak.network.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


enum class RECORDING_STATE {
    START, PAUSE, STOP
}

enum class TRANSLATING_STATE {
    START, STOP
}

enum class MESSAGE_ROLE {
    SYSTEM, ASSISTANT, USER
}

class ConversationFragment : Fragment(), TextToSpeech.OnInitListener {

    private var recorder: MediaRecorder? = null
    private var translator: MediaRecorder? = null

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var recordingCacheFile: File
    private lateinit var translatingCacheFile: File
    private lateinit var syntheticCacheFile: File

    private var textToSpeech: TextToSpeech? = null

    private var binding: FragmentChatBinding? = null

    lateinit var sharedPref: SharedPreferences

    private val sharedViewModel: MainViewModel by activityViewModels()

    private val viewModel: ChatViewModel by viewModels<ChatViewModel> {
        ChatViewModelFactory((requireContext().applicationContext as FluentSpeakApplication).apisRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chat,
            container,
            false
        )

        // TextToSpeech(Context: this, OnInitListener: this)
        textToSpeech = TextToSpeech(requireContext(), this)

        sharedPref = context?.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )!!

        setupListeners()

        viewModel.currentConversation.observe(viewLifecycleOwner, Observer {
            // If user changed another conversation
            if (viewModel.previousConversation.value != it) {
                viewModel.updatePreviousConversation()
                changeSubredditOrConversation()
            }
        })

        sharedViewModel.conversations.observe(viewLifecycleOwner, Observer {
            // If user selected another subreddit
            if (viewModel.conversations.value != it) {
                viewModel.setConversations(it)
                changeSubredditOrConversation()
            }
        })

        return binding?.root
    }

    fun setupListeners() {
        binding?.recordButton?.setOnClickListener {

            when (viewModel.currentRecordingState) {
                RECORDING_STATE.START -> {
                    stopRecordingRecorder()

                    disableButtons()

                    lifecycleScope.launch {
                        val text = speechToText()

                        val userMessagePortion = Message(
                            MESSAGE_ROLE.USER.toString().lowercase(),
                            text.trim()
                        )

                        viewModel.addMessageToUnfinishedMessage(userMessagePortion)

                        addMessagesToView(userMessagePortion)

                        updateButtons(true, true, true, recordIcon = "play")
                    }
                    viewModel.setCurrentRecordingState(RECORDING_STATE.PAUSE)
                }

                RECORDING_STATE.STOP -> {
                    recorder?.start()
                    updateButtons(true, true, false, recordIcon = "pause")
                    viewModel.setCurrentRecordingState(RECORDING_STATE.START)
                }

                RECORDING_STATE.PAUSE -> {
                    recorder?.start()
                    updateButtons(true, true, false, recordIcon = "pause")
                    viewModel.setCurrentRecordingState(RECORDING_STATE.START)
                }
            }
        }

        binding?.stopButton?.setOnClickListener {
            when (viewModel.currentRecordingState) {
                RECORDING_STATE.START -> {
                    stopRecordingRecorder()

                    disableButtons()

                    lifecycleScope.launch {
                        val text = speechToText()

                        val userMessagePortion = Message(
                            MESSAGE_ROLE.USER.toString().lowercase(),
                            text.trim()
                        )

                        viewModel.addMessageToUnfinishedMessage(userMessagePortion)

                        addMessagesToView(userMessagePortion)

                        val userName = sharedPref.getString(context?.getString(R.string.username_key), "")
                            .toString()

                        val userMessageFormatted =
                            userName + " said: " + viewModel.unfinishedMessage.content

                        viewModel.addMessages(
                            Message(
                                MESSAGE_ROLE.USER.toString().lowercase(),
                                userMessageFormatted
                            )
                        )
                        viewModel.cleanUnfinishedMessage()

                        val lastMessages = viewModel.messages.takeLast(getMessagesToChatGPT())
                        val randomSpeaker = getLessFrequentSpeaker(lastMessages)

                        val messages = listOf(viewModel.systemMessage) + lastMessages + listOf(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), randomSpeaker.name + " said: "))

                        val chatCompletionResponse = getChatCompletionResponse(messages)

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            randomSpeaker.name + " said: " + chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessages(chatCompletionMessage)

                        addMessagesToView(chatCompletionMessage)

                        val dialogues = getDialogues(chatCompletionMessage)

                        try {
                            textToSpeech(
                                *dialogues,
                                updateButtons = { updateButtons(true, false, true, recordIcon = "mic") }
                            )
                        } catch (e: HttpException) {
                            Log.e("ChatFragment: ", e.toString())
                            textToSpeech?.speak(
                                chatCompletionMessage.content,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                            updateButtons(true, false, true, recordIcon = "mic")
                        } catch (e: IOException) {
                            Log.e("ChatFragment: ", e.toString())
                            updateButtons(true, false, true, recordIcon = "mic")
                        }
                    }
                    viewModel.setCurrentRecordingState(RECORDING_STATE.STOP)
                }

                RECORDING_STATE.STOP -> return@setOnClickListener
                RECORDING_STATE.PAUSE -> {
                    recorder?.reset()

                    configureRecorder()

                    val userName = sharedPref.getString(context?.getString(R.string.username_key), "")
                        .toString()

                    val userMessageFormatted =
                        userName + " said: " + viewModel.unfinishedMessage.content

                    viewModel.addMessages(
                        Message(
                            MESSAGE_ROLE.USER.toString().lowercase(),
                            userMessageFormatted
                        )
                    )
                    viewModel.cleanUnfinishedMessage()

                    val lastMessages = viewModel.messages.takeLast(getMessagesToChatGPT())
                    val randomSpeaker = getLessFrequentSpeaker(lastMessages)

                    val messages = listOf(viewModel.systemMessage) + lastMessages + listOf(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), randomSpeaker.name + " said: "))

                    disableButtons()

                    lifecycleScope.launch {
                        val chatCompletionResponse = getChatCompletionResponse(messages)

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            randomSpeaker.name + " said: " + chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessages(chatCompletionMessage)

                        addMessagesToView(chatCompletionMessage)

                        val dialogues = getDialogues(chatCompletionMessage)

                        try {
                            textToSpeech(
                                *dialogues, updateButtons = { updateButtons(true, false, true) }
                            )
                        } catch (e: HttpException) {
                            Log.e("ChatFragment: ", e.toString())
                            textToSpeech?.speak(
                                chatCompletionMessage.content,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                            updateButtons(true, false, true)
                        } catch (e: IOException) {
                            Log.e("ChatFragment: ", e.toString())
                            updateButtons(true, false, true)
                        }
                    }
                    viewModel.setCurrentRecordingState(RECORDING_STATE.STOP)
                }
            }
        }

        binding?.translateButton?.setOnClickListener {
            when (viewModel.currentRecordingState) {
                RECORDING_STATE.START -> {
                    return@setOnClickListener
                }

                RECORDING_STATE.STOP -> {
                    when (viewModel.currentTranslatingState) {
                        TRANSLATING_STATE.STOP -> {
                            translator?.start()
                            updateButtons(false, false, true, translateIcon = "stop")
                            viewModel.setCurrentTranslatingState(TRANSLATING_STATE.START)
                        }

                        TRANSLATING_STATE.START -> {
                            stopRecordingTranslator()

                            disableButtons()

                            lifecycleScope.launch {
                                val text = speechToEnglishText()

                                try {
                                    textToSpeech(
                                        Pair(
                                            Input(text),
                                            null
                                        ), updateButtons = { updateButtons(true, false, true, translateIcon = "translate") }
                                    )
                                } catch (e: HttpException) {
                                    Log.e("ChatFragment: ", e.toString())
                                    textToSpeech?.speak(
                                        text,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        ""
                                    )
                                    updateButtons(true, false, true, translateIcon = "translate")
                                } catch (e: IOException) {
                                    Log.e("ChatFragment: ", e.toString())
                                    updateButtons(true, false, true, translateIcon = "translate")
                                }
                            }
                            viewModel.setCurrentTranslatingState(TRANSLATING_STATE.STOP)
                        }
                    }
                }

                RECORDING_STATE.PAUSE -> {
                    when (viewModel.currentTranslatingState) {
                        TRANSLATING_STATE.STOP -> {
                            translator?.start()
                            updateButtons(false, false, true, translateIcon = "stop")
                            viewModel.setCurrentTranslatingState(TRANSLATING_STATE.START)
                        }

                        TRANSLATING_STATE.START -> {
                            stopRecordingTranslator()

                            disableButtons()

                            lifecycleScope.launch {
                                val text = speechToEnglishText()

                                try {
                                    textToSpeech(
                                        Pair(
                                            Input(text),
                                            null
                                        ), updateButtons = { updateButtons(true, true, true, translateIcon = "translate") }
                                    )
                                } catch (e: HttpException) {
                                    Log.e("ChatFragment: ", e.toString())
                                    textToSpeech?.speak(
                                        text,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        ""
                                    )
                                    updateButtons(true, true, true, translateIcon = "translate")
                                } catch (e: IOException) {
                                    Log.e("ChatFragment: ", e.toString())
                                    updateButtons(true, true, true, translateIcon = "translate")
                                }
                            }
                            viewModel.setCurrentTranslatingState(TRANSLATING_STATE.STOP)
                        }
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    createRecorder()
                    createTranslator()

                    configureRecorder()
                    configureTranslator()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Snackbar.make(
                        requireView(),
                        "Voice recording does not work without the permission",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        when {
            shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                Snackbar.make(
                    requireView(),
                    "Voice recording does not work without the permission",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    android.Manifest.permission.RECORD_AUDIO
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        mediaPlayer = MediaPlayer()
        syntheticCacheFile = File.createTempFile("synthetic_voice.mp3", null, context?.cacheDir)

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                createRecorder()
                createTranslator()

                configureRecorder()
                configureTranslator()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null

        translator?.release()
        translator = null

        mediaPlayer?.release()
        mediaPlayer = null

        syntheticCacheFile.delete()
        recordingCacheFile.delete()
        translatingCacheFile.delete()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()

    }

    suspend fun getChatCompletionResponse(messages: List<Message>): ChatCompletionResponse {
        return withContext(Dispatchers.IO) {
            viewModel.getChatCompletionResponse(
                ChatCompletionRequestData(
                    messages = messages,
                    temperature = sharedPref.getFloat(
                        context?.getString(R.string.chat_gpt_temperature_key),
                        0.0f
                    ),
                    maxTokens = sharedPref.getInt(
                        context?.getString(R.string.chat_gpt_max_tokens_key),
                        0
                    ),
                    presencePenalty = sharedPref.getFloat(
                        context?.getString(R.string.chat_gpt_presence_penalty_key),
                        0.0f
                    ),
                    frequencyPenalty = sharedPref.getFloat(
                        context?.getString(R.string.chat_gpt_frecuency_penalty_key),
                        0.0f
                    ),
                )
            )
        }
    }

    suspend fun getTextToSpeechResponse(
        input: Input,
        voice: Voice = Voice(
            sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "")
                .toString(),
            sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "")
                .toString(),
            sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "")
                .toString()
        )
    ): TextToSpeechResponse {
        return withContext(Dispatchers.IO) {
            viewModel.getTextToSpeechResponse(TextToSpeechRequestData(input, voice))
        }
    }

    suspend fun getTranscriptionResponse(): TranscriptionResponse {
        return withContext(Dispatchers.IO) {
            viewModel.getTranscriptionResponse(
                TranscriptionRequestData(
                    file = recordingCacheFile,
                    prompt = viewModel.unfinishedMessage.content,
                    temperature = sharedPref.getFloat(
                        context?.getString(R.string.whisper_temperature_key),
                        0.0f
                    )
                )
            )
        }
    }

    suspend fun getTranslationResponse(): TranslationResponse {
        return withContext(Dispatchers.IO) {
            viewModel.getTranslationResponse(
                TranslationRequestData(
                    file = translatingCacheFile,
                    temperature = sharedPref.getFloat(
                        context?.getString(R.string.whisper_temperature_key),
                        0.0f
                    )
                )
            )
        }
    }

    suspend private fun textToSpeech(
        vararg array: Pair<Input, Voice?>,
        updateButtons: () -> Unit
    ) {
        for (i in array.indices) {
            var textToSpeechResponse: TextToSpeechResponse

            if (array[i].second == null) {
                textToSpeechResponse = getTextToSpeechResponse(array[i].first)
            } else {
                textToSpeechResponse = getTextToSpeechResponse(array[i].first, array[i].second!!)
            }

            val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

            if (i == 0) {
                writeDataToFile(dataDecoded, syntheticCacheFile)
            } else {
                writeDataToFile(dataDecoded, syntheticCacheFile, true)
            }
        }

        configurePlayer()

        startPlayer()

        resetUntilFinishedPlaying(updateButtons)
    }

    suspend private fun speechToText(): String {
        val transcriptionResponse = getTranscriptionResponse()

        configureRecorder()

        return transcriptionResponse.text
    }

    suspend private fun speechToEnglishText(): String {
        val translationResponse = getTranslationResponse()

        configureTranslator()

        return translationResponse.text
    }

    private fun createRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = MediaRecorder(requireContext())
        } else {
            recorder = MediaRecorder()
        }
    }

    private fun createTranslator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            translator = MediaRecorder(requireContext())
        } else {
            translator = MediaRecorder()
        }
    }

    private fun stopRecordingRecorder() {
        try {
            recorder?.stop()
        } catch (runtimeException: java.lang.RuntimeException) {
            recordingCacheFile.delete()
            createRecorder()
        }
    }

    private fun stopRecordingTranslator() {
        try {
            translator?.stop()
        } catch (runtimeException: java.lang.RuntimeException) {
            translatingCacheFile.delete()
            createTranslator()
        }
    }

    private fun configureRecorder() {
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            recordingCacheFile = File.createTempFile("recording.m4a", null, context?.cacheDir)

            setOutputFile(recordingCacheFile.absolutePath)

            prepare()
        }
    }

    private fun configureTranslator() {
        translator?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            translatingCacheFile = File.createTempFile("translation.m4a", null, context?.cacheDir)

            setOutputFile(translatingCacheFile.absolutePath)

            prepare()
        }
    }

    internal fun configurePlayer() {
        mediaPlayer?.apply {
            setDataSource(syntheticCacheFile.absolutePath)
            prepare()
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
    }

    private fun resetUntilFinishedPlaying(updateButtons: () -> Unit) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.reset()
                updateButtons()
            }
        } else {
            mediaPlayer?.reset()
            updateButtons()
        }
    }

    private fun writeDataToFile(data: ByteArray, file: File, append: Boolean = false) {
        val fos = FileOutputStream(file, append)
        fos.write(data)
        fos.close()
    }

    private fun disableButtons() {
        binding?.recordButton?.setEnabled(false)
        binding?.stopButton?.setEnabled(false)
        binding?.translateButton?.setEnabled(false)
    }

    private fun updateButtons(
        recordEnabled: Boolean,
        stopEnabled: Boolean,
        translateEnabled: Boolean,
        recordIcon: String = "",
        translateIcon: String = ""
    ) {
        val list = listOf(
            Pair(binding?.recordButton, recordEnabled),
            Pair(binding?.stopButton, stopEnabled),
            Pair(binding?.translateButton, translateEnabled)
        )

        for (element in list) {
            if (element.second) {
                element.first?.setEnabled(true)
            } else {
                element.first?.setEnabled(false)
            }
        }

        when (recordIcon) {
            "mic" ->
                (binding?.recordButton as MaterialButton)?.icon =
                    resources.getDrawable(R.drawable.baseline_mic_24, null)
            "play" ->
                (binding?.recordButton as MaterialButton).icon =
                    resources.getDrawable(R.drawable.baseline_play_arrow_24, null)
            "pause" ->
                (binding?.recordButton as MaterialButton).icon =
                    resources.getDrawable(R.drawable.baseline_pause_24, null)
        }

        when (translateIcon) {
            "translate" -> (binding?.translateButton as MaterialButton).icon =
                resources.getDrawable(R.drawable.baseline_translate_24, null)
            "stop" -> (binding?.translateButton as MaterialButton).icon =
                resources.getDrawable(R.drawable.baseline_stop_24, null)
        }
    }

    private fun getSpeakerAndPost(message: Message): Pair<String, String> {
        val regex1 = Regex("[A-Z][a-z]+(?= said: )")
        val regex2 = Regex("(?<= said: )(.|\n)+")

        val speakerName = regex1.find(message.content)?.value ?: ""
        val post = regex2.find(message.content)?.value ?: ""

        return Pair(speakerName, post)
    }

    internal fun addMessagesToView(vararg messages: Message): Int {

        var previousSpeakerName = ""

        for (message in messages) {
            if (message.content == "") {
                continue
            }

            val messageView = TextView(context)

            if (message.role.equals(MESSAGE_ROLE.SYSTEM.toString().lowercase())) {
                messageView.text = message.content
            } else if (message.role.equals(MESSAGE_ROLE.USER.toString().lowercase())) {
                messageView.text = message.content
            } else if (message.role.equals(MESSAGE_ROLE.ASSISTANT.toString().lowercase())) {
                val (speakerName, post) = getSpeakerAndPost(message)

                lateinit var textColor: String

                for (speaker in viewModel.speakers) {
                    if (speaker.name.equals(speakerName)) {
                        textColor = speaker.textColor
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (previousSpeakerName.equals(speakerName)) {
                        messageView.text = Html.fromHtml(post, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                    } else {
                        messageView.text = Html.fromHtml("<b><font color=\"" + textColor + "\">" + speakerName + "</b></font><br>" + post, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                    }
                    previousSpeakerName = speakerName!!
                }
            }

            messageView.textSize = 16f
            messageView.setPadding(16, 16, 16, 16)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams.setMargins(16, 16, 16, 16)

            val screenWidth = Resources.getSystem().displayMetrics.widthPixels

            when (message.role) {
                MESSAGE_ROLE.SYSTEM.toString().lowercase() -> {
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                    messageView.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.round_corner_textview_system
                    )
                    messageView.setTextColor(Color.WHITE)
                }

                MESSAGE_ROLE.ASSISTANT.toString().lowercase() -> {
                    layoutParams.gravity = Gravity.START
                    messageView.maxWidth = (screenWidth * 0.6).toInt()
                    messageView.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.round_corner_textview_assistant
                    )
                }

                MESSAGE_ROLE.USER.toString().lowercase() -> {
                    layoutParams.gravity = Gravity.END
                    messageView.maxWidth = (screenWidth * 0.6).toInt()
                    messageView.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.round_corner_textview_user
                    )
                }
            }

            messageView.layoutParams = layoutParams

            binding?.chatLayout?.addView(messageView)
        }
        val id = (1000..9000).random()

        return id
    }

    private fun getDialogues(message: Message): Array<Pair<Input, Voice?>> {
        var dialogues = arrayOf<Pair<Input,Voice?>>()

        val (speakerName, post) = getSpeakerAndPost(message)

        dialogues += Pair(Input(speakerName + " said: "), null)

        lateinit var voice: Voice

        for (speaker in viewModel.speakers) {
            if (speakerName.equals(speaker.name)) {
                voice = speaker.voice
            }
        }

        dialogues += Pair(Input(post), voice)

        return dialogues
    }

    private fun getMessagesToChatGPT(): Int {

        val utterancesAtBeginning = sharedPref.getInt(
            context?.getString(R.string.utterances_at_beginning_key),
            0
        )
        val CONVERSATION_TITLE = 1
        val STARTER_UTTERANCE = 1
        val USER_RESPONSE = 1
        val messagesToChatGPT =
            utterancesAtBeginning + CONVERSATION_TITLE + STARTER_UTTERANCE + USER_RESPONSE

        return messagesToChatGPT
    }

    private fun decodeBase64ToByteArray(encodedBase64: String): ByteArray {
        return android.util.Base64.decode(encodedBase64, android.util.Base64.DEFAULT)
    }

    private fun createSpeakers(starterUtterance: Utterance, utterances: MutableList<Utterance>) {
        // Delete previous speakers
        viewModel.cleanSpeakers()

        // Filter different voices from the voice in shared preferences for female and male

        val femaleVoices = TextToSpeechSettingsData.VOICES.filter {
            if (!it.equals(Voice(
                    sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "")
                        .toString(),
                    sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "")
                        .toString(),
                    sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "")
                        .toString()
                ))) {
                it.ssmlGender.equals("FEMALE")
            } else {
                false
            }
        }.toMutableList()

        val maleVoices = TextToSpeechSettingsData.VOICES.filter {
            if (!it.equals(Voice(
                    sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "")
                        .toString(),
                    sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "")
                        .toString(),
                    sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "")
                        .toString()
                ))) {
                it.ssmlGender.equals("MALE")
            } else {
                false
            }
        }.toMutableList()

        val textColors = mutableListOf<String>(getString(R.string.first_color), getString(R.string.second_color), getString(R.string.third_color), getString(R.string.fourth_color), getString(R.string.fifth_color))

        val speakers = mutableListOf<Pair<String, String>>()

        speakers.add(Pair(starterUtterance.speaker, starterUtterance.gender))

        for (utterance in utterances) {
            speakers.add(Pair(utterance.speaker, utterance.gender))
        }

        val uniqueSpeakers = speakers.toSet()

        for (uniqueSpeaker in uniqueSpeakers) {
            val name = uniqueSpeaker.first
            val gender = uniqueSpeaker.second
            val textColor = textColors.asSequence().shuffled().take(1).toList()[0]

            textColors.remove(textColor)

            lateinit var voice: Voice
            if (uniqueSpeaker.second.equals("male")) {
                voice = maleVoices.asSequence().shuffled().take(1).toList()[0]

                maleVoices.remove(voice)
            } else if (uniqueSpeaker.second.equals("female")) {
                voice = femaleVoices.asSequence().shuffled().take(1).toList()[0]

                femaleVoices.remove(voice)
            }

            val speaker = Speaker(name, gender, textColor, voice)

            viewModel.addSpeaker(speaker)
        }
    }

    private fun getLessFrequentSpeaker(messages: List<Message>): Speaker {
        var minFrecuency = 999
        var lessFrequentSpeakers = mutableListOf<Speaker>()
        for (speaker in viewModel.speakers) {
            val frecuency = messages.count { it.content.contains(speaker.name + " said: ") }

            if (frecuency < minFrecuency) {
                minFrecuency = frecuency
                lessFrequentSpeakers.clear()
                lessFrequentSpeakers.add(speaker)
            } else if (frecuency == minFrecuency) {
                lessFrequentSpeakers.add(speaker)
            }
        }

        return lessFrequentSpeakers.asSequence().shuffled().take(1).toList()[0]
    }

    private fun changeSubredditOrConversation() {
        binding?.chatLayout?.removeAllViews()
        viewModel.cleanMessages()

        addMessagesToView(viewModel.systemMessage)

        val currentConversation = viewModel.currentConversation.value ?: 0

        val conversations =
            viewModel.conversations.value ?: listOf<ConversationWithUtterances>()

        // Extract starter utterance and utterances

        val conversation = conversations[currentConversation].conversation

        lateinit var starterUtterance: Utterance
        for (utterance in conversations[currentConversation].utterances) {
            if (utterance.replyTo == null) {
                starterUtterance = utterance
                break
            }
        }

        var utterances = mutableListOf<Utterance>()
        for (utterance in conversations[currentConversation].utterances) {
            if (utterance.replyTo != null) {
                utterances.add(utterance)
            }
        }

        val userName = sharedPref.getString(context?.getString(R.string.username_key), "")
            .toString()

        utterances = utterances.filter { !it.speaker.equals(userName) }.toMutableList()

        val utterancesAtBeginning = sharedPref.getInt(
            context?.getString(R.string.utterances_at_beginning_key),
            0
        )

        utterances = utterances.asSequence().shuffled().take(utterancesAtBeginning)
            .toMutableList()

        // Create speakers
        createSpeakers(starterUtterance, utterances)

        // Add title and starter utterance

        val conversationTitleFormatted =
            starterUtterance.speaker + " said: " + conversation.title
        val starterUtteranceFormatted =
            starterUtterance.speaker + " said: " + starterUtterance.text

        addMessagesToView(
            Message(
                MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                conversationTitleFormatted
            ),
            Message(
                MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                starterUtteranceFormatted
            )
        )

        viewModel.addMessages(
            Message(
                MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                conversationTitleFormatted
            ),
            Message(
                MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                starterUtteranceFormatted
            )
        )

        var dialogues = arrayOf<Pair<Input, Voice?>>()

        dialogues += Pair(Input(starterUtterance.speaker + " said: "), null)

        for (speaker in viewModel.speakers) {
            if (starterUtterance.speaker.equals(speaker.name)) {
                dialogues += Pair(Input(conversation.title + " " + starterUtterance.text), speaker.voice)
            }
        }

        var previousSpeakerName = ""

        // Add utterances
        for (utterance in utterances) {
            val utteranceFormatted = utterance.speaker + " said: " + utterance.text

            viewModel.addMessages(
                Message(
                    MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                    utteranceFormatted
                )
            )

            addMessagesToView(
                Message(
                    MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                    utteranceFormatted
                )
            )

            if (!previousSpeakerName.equals(utterance.speaker)) {
                dialogues += Pair(Input(utterance.speaker + " said: "), null)
            }

            for (speaker in viewModel.speakers) {
                if (utterance.speaker.equals(speaker.name)) {
                    dialogues += Pair(Input(utterance.text), speaker.voice)
                }
            }

            previousSpeakerName = utterance.speaker
        }

        viewModel.cleanUnfinishedMessage()

        disableButtons()

        lifecycleScope.launch {
            try {
                textToSpeech(
                    *dialogues,
                    updateButtons = { updateButtons(true, false, true) }
                )
            } catch (e: HttpException) {
                Log.e("ChatFragment: ", e.toString())
                textToSpeech?.speak(
                    conversationTitleFormatted + starterUtterance.text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                updateButtons(true, false, true)
            } catch (e: IOException) {
                Log.e("ChatFragment: ", e.toString())
                updateButtons(true, false, true)
            }
        }
        viewModel.setCurrentRecordingState(RECORDING_STATE.STOP)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater: MenuInflater = inflater
        inflater.inflate(R.menu.conversation_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settingsFragment -> {
                val navController = requireActivity().findNavController(R.id.nav_host_fragment)

                return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(
                    item
                )
            }

            R.id.item_change_conversation -> {
                viewModel.nextConversation()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            }
        }
    }
}