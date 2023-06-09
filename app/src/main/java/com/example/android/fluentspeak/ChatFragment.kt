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

val UTTERANCES_PER_CONVERSATION = 4
val CONVERSATION_TITLE = 1
val STARTER_UTTERANCE = 1
val USER_RESPONSE = 1
val MESSAGES_TO_CHATGPT =
    UTTERANCES_PER_CONVERSATION + CONVERSATION_TITLE + STARTER_UTTERANCE + USER_RESPONSE

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
            if (viewModel.previousConversation.value != it) {

                viewModel.updatePreviousConversation()

                binding?.chatLayout?.removeAllViews()
                viewModel.cleanMessages()

                addMessagesToView(viewModel.systemMessage)

                val currentConversation = it

                val conversations =
                    viewModel.conversations.value ?: listOf<ConversationWithUtterances>()

                // Add title and starter utterance

                val conversation = conversations[currentConversation].conversation

                lateinit var starterUtterance: Utterance
                for (utterance in conversations[currentConversation].utterances) {
                    if (utterance.replyTo == null) {
                        starterUtterance = utterance
                        break
                    }
                }

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
                        starterUtterance.text
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

                // Add utterances


                var utterances = mutableListOf<Utterance>()
                for (utterance in conversations[currentConversation].utterances) {
                    if (utterance.replyTo != null) {
                        utterances.add(utterance)
                    }
                }

                utterances = utterances.asSequence().shuffled().take(UTTERANCES_PER_CONVERSATION)
                    .toMutableList()

                for (utterance in utterances) {
                    val utteranceFormatted = utterance.speaker + " said: " + utterance.text

                    viewModel.addMessages(
                        Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            utteranceFormatted
                        )
                    )
                }

                viewModel.cleanUnfinishedMessage()

                disableButtons()

                lifecycleScope.launch {
                    try {
                        textToSpeech(
                            Pair(
                                Input(conversationTitleFormatted + starterUtterance.text),
                                null
                            ), updateButtons = { updateButtons(true, false, true) }
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
        })

        sharedViewModel.conversations.observe(viewLifecycleOwner, Observer {

            // If user selected another subreddit
            if (viewModel.conversations.value != it) {
                viewModel.setConversations(it)

                binding?.chatLayout?.removeAllViews()

                addMessagesToView(viewModel.systemMessage)

                val currentConversation = viewModel.currentConversation.value ?: 0

                // Add title and starter utterance

                val conversation = it[currentConversation].conversation

                lateinit var starterUtterance: Utterance
                for (utterance in it[currentConversation].utterances) {
                    if (utterance.replyTo == null) {
                        starterUtterance = utterance
                        break
                    }
                }

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
                        starterUtterance.text
                    )
                )

                viewModel.cleanMessages()

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

                // Add utterances

                var utterances = mutableListOf<Utterance>()
                for (utterance in it[currentConversation].utterances) {
                    if (utterance.replyTo != null) {
                        utterances.add(utterance)
                    }
                }

                utterances = utterances.asSequence().shuffled().take(UTTERANCES_PER_CONVERSATION)
                    .toMutableList()

                for (utterance in utterances) {
                    val utteranceFormatted = utterance.speaker + " said: " + utterance.text

                    viewModel.addMessages(
                        Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            utteranceFormatted
                        )
                    )
                }

                viewModel.cleanUnfinishedMessage()

                lifecycleScope.launch {
                    try {
                        textToSpeech(
                            Pair(
                                Input(conversationTitleFormatted + starterUtterance.text),
                                null
                            ), updateButtons = { updateButtons(true, false, true) }
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

                        val userMessageFormatted =
                            "Orlando" + " said: " + viewModel.unfinishedMessage.content

                        viewModel.addMessages(
                            Message(
                                MESSAGE_ROLE.USER.toString().lowercase(),
                                userMessageFormatted
                            )
                        )
                        viewModel.cleanUnfinishedMessage()

                        val lastMessages = viewModel.messages.takeLast(MESSAGES_TO_CHATGPT)
                        val messages = listOf(viewModel.systemMessage) + lastMessages

                        val chatCompletionResponse = getChatCompletionResponse(messages)

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessages(chatCompletionMessage)

                        addMessagesToView(chatCompletionMessage)

                        try {
                            textToSpeech(
                                Pair(
                                    Input(chatCompletionMessage.content),
                                    null
                                ), updateButtons = { updateButtons(true, false, true, recordIcon = "mic") }
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

                    val userMessageFormatted =
                        "Orlando" + " said: " + viewModel.unfinishedMessage.content

                    viewModel.addMessages(
                        Message(
                            MESSAGE_ROLE.USER.toString().lowercase(),
                            userMessageFormatted
                        )
                    )
                    viewModel.cleanUnfinishedMessage()

                    val lastMessages = viewModel.messages.takeLast(MESSAGES_TO_CHATGPT)
                    val messages = listOf(viewModel.systemMessage) + lastMessages

                    disableButtons()

                    lifecycleScope.launch {
                        val chatCompletionResponse = getChatCompletionResponse(messages)

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessages(chatCompletionMessage)

                        addMessagesToView(chatCompletionMessage)

                        try {
                            textToSpeech(
                                Pair(
                                    Input(chatCompletionMessage.content),
                                    null
                                ), updateButtons = { updateButtons(true, false, true) }
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

    internal fun addMessagesToView(vararg messages: Message): Int {
        for (message in messages) {
            if (message.content == "") {
                continue
            }

            val messageView = TextView(context)

            messageView.text = message.content
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
            /*
            messageView.id = (1000..9000).random()

            return messageView.id*/
        }
        return TODO("Provide the return value")
    }

    private fun decodeBase64ToByteArray(encodedBase64: String): ByteArray {
        return android.util.Base64.decode(encodedBase64, android.util.Base64.DEFAULT)
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