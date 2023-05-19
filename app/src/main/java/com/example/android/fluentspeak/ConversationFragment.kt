package com.example.android.fluentspeak

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.android.fluentspeak.database.ConversationWithUtterances
import com.example.android.fluentspeak.database.Utterance
import com.example.android.fluentspeak.databinding.FragmentConversationBinding
import com.example.android.fluentspeak.network.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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
val MESSAGES_TO_CHATGPT = UTTERANCES_PER_CONVERSATION + CONVERSATION_TITLE + STARTER_UTTERANCE + USER_RESPONSE

class ConversationFragment : Fragment() {

    private var currentRecordingState = RECORDING_STATE.STOP
    private var currentTranslatingState = TRANSLATING_STATE.STOP

    private var recorder: MediaRecorder? = null
    private var translator: MediaRecorder? = null

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var recordingCacheFile: File
    private lateinit var translatingCacheFile: File
    private lateinit var syntheticCacheFile: File

    private var chatLayout: LinearLayout? = null

    lateinit var sharedPref: SharedPreferences

    private val sharedViewModel: MainViewModel by activityViewModels()

    private val viewModel: ConversationViewModel by viewModels<ConversationViewModel> {
        ConversationViewModelFactory((requireContext().applicationContext as FluentSpeakApplication).apisRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentConversationBinding.inflate(inflater)

        sharedPref = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)!!

        chatLayout = binding.chatLayout

        addMessageToView(viewModel.systemMessage)

        setupListeners(binding)

        sharedViewModel.conversations.observe(viewLifecycleOwner, Observer {

            if (viewModel.conversations.value != it) {
                viewModel.setConversations(it)

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

                val conversationTitleFormatted = starterUtterance.speaker + " said: " + conversation.title
                val starterUtteranceFormatted = starterUtterance.speaker + " said: " + starterUtterance.text

                addMessageToView(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), conversationTitleFormatted))
                addMessageToView(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), starterUtterance.text))

                viewModel.cleanMessages()

                viewModel.addMessage(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), conversationTitleFormatted))
                viewModel.addMessage(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), starterUtteranceFormatted))

                // Add utterances

                var utterances = mutableListOf<Utterance>()
                for (utterance in it[currentConversation].utterances) {
                    if (utterance.replyTo != null) {
                        utterances.add(utterance)
                    }
                }

                utterances = utterances.asSequence().shuffled().take(UTTERANCES_PER_CONVERSATION).toMutableList()

                for (utterance in utterances) {
                    val utteranceFormatted = utterance.speaker + " said: " + utterance.text

                    addMessageToView(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), utteranceFormatted))

                    viewModel.addMessage(Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), utteranceFormatted))
                }

                viewModel.cleanUnfinishedMessage()

                disableButtons(binding)

                val updateButtons = {
                    binding.recordButton.setEnabled(true)
                    binding.stopButton.setEnabled(false)
                    binding.translateButton.setEnabled(true)
                }

                lifecycleScope.launch {
                    val textToSpeechResponse = withContext(Dispatchers.IO) {
                        viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(conversationTitleFormatted + starterUtterance.text), Voice(
                            sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "").toString(),
                            sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "").toString(),
                            sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "").toString()
                        )))
                    }

                    val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                    writeDataToFile(dataDecoded, syntheticCacheFile)

                    configurePlayer()

                    startPlayer()
                    resetUntilFinishedPlaying(updateButtons)
                }
                currentRecordingState = RECORDING_STATE.STOP
            } else {
                for (message in viewModel.messages) {
                    addMessageToView(message)
                }
            }
        })

        return binding.root
    }

    fun setupListeners(binding: FragmentConversationBinding) {
        binding.recordButton.setOnClickListener {

            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    stopRecordingRecorder()

                    disableButtons(binding)

                    val updateButtons = {
                        (it as MaterialButton).icon =
                            resources.getDrawable(R.drawable.baseline_play_arrow_24, null)
                        binding.recordButton.setEnabled(true)
                        binding.stopButton.setEnabled(true)
                        binding.translateButton.setEnabled(true)
                    }

                    lifecycleScope.launch {
                        val transcriptionResponse = withContext(Dispatchers.IO) {
                            viewModel.getTranscriptionResponse(TranscriptionRequestData(
                                file = recordingCacheFile,
                                prompt = viewModel.unfinishedMessage.content,
                                temperature = sharedPref.getFloat(context?.getString(R.string.whisper_temperature_key), 0.0f)
                            ))
                        }

                        configureRecorder()

                        val userMessagePortion = Message(MESSAGE_ROLE.USER.toString().lowercase(), transcriptionResponse.text.trim())

                        viewModel.addMessageToUnfinishedMessage(userMessagePortion)

                        addMessageToView(userMessagePortion)

                        updateButtons()
                    }
                    currentRecordingState = RECORDING_STATE.PAUSE
                }
                RECORDING_STATE.STOP -> {
                    recorder?.start()
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_pause_24, null)
                    binding.recordButton.setEnabled(true)
                    binding.stopButton.setEnabled(true)
                    binding.translateButton.setEnabled(false)
                    currentRecordingState = RECORDING_STATE.START
                }
                RECORDING_STATE.PAUSE -> {
                    recorder?.start()
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_pause_24, null)
                    binding.recordButton.setEnabled(true)
                    binding.stopButton.setEnabled(true)
                    binding.translateButton.setEnabled(false)
                    currentRecordingState = RECORDING_STATE.START
                }
            }
        }

        binding.stopButton.setOnClickListener {
            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    stopRecordingRecorder()

                    disableButtons(binding)

                    val updateButtons = {
                        (binding.recordButton).icon =
                            resources.getDrawable(R.drawable.baseline_mic_24, null)
                        binding.recordButton.setEnabled(true)
                        binding.stopButton.setEnabled(false)
                        binding.translateButton.setEnabled(true)
                    }

                    lifecycleScope.launch {
                        val transcriptionResponse = withContext(Dispatchers.IO) {
                            viewModel.getTranscriptionResponse(TranscriptionRequestData(
                                file = recordingCacheFile,
                                prompt = viewModel.unfinishedMessage.content,
                                temperature = sharedPref.getFloat(context?.getString(R.string.whisper_temperature_key), 0.0f)
                            ))
                        }

                        configureRecorder()

                        val userMessagePortion = Message(MESSAGE_ROLE.USER.toString().lowercase(), transcriptionResponse.text.trim())

                        viewModel.addMessageToUnfinishedMessage(userMessagePortion)

                        addMessageToView(userMessagePortion)

                        val userMessageFormatted = "Orlando" + " said: " + viewModel.unfinishedMessage.content

                        viewModel.addMessage(Message(MESSAGE_ROLE.USER.toString().lowercase(), userMessageFormatted))
                        viewModel.cleanUnfinishedMessage()

                        val lastMessages = viewModel.messages.takeLast(MESSAGES_TO_CHATGPT)
                        val messages = listOf(viewModel.systemMessage) + lastMessages

                        val chatCompletionResponse = withContext(Dispatchers.IO) {
                            viewModel.getChatCompletionResponse(ChatCompletionRequestData(
                                messages = messages,
                                temperature = sharedPref.getFloat(context?.getString(R.string.chat_gpt_temperature_key), 0.0f),
                                maxTokens = sharedPref.getInt(context?.getString(R.string.chat_gpt_max_tokens_key), 0),
                                presencePenalty = sharedPref.getFloat(context?.getString(R.string.chat_gpt_presence_penalty_key), 0.0f),
                                frequencyPenalty = sharedPref.getFloat(context?.getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f),
                            ))
                        }

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessage(chatCompletionMessage)

                        addMessageToView(chatCompletionMessage)

                        val textToSpeechResponse = withContext(Dispatchers.IO) {
                            viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(chatCompletionMessage.content), Voice(
                                sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "").toString(),
                                sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "").toString(),
                                sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "").toString()
                                )))
                        }

                        val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                        writeDataToFile(dataDecoded, syntheticCacheFile)

                        configurePlayer()

                        startPlayer()
                        resetUntilFinishedPlaying(updateButtons)
                    }
                    currentRecordingState = RECORDING_STATE.STOP
                }
                RECORDING_STATE.STOP -> return@setOnClickListener
                RECORDING_STATE.PAUSE -> {
                    recorder?.reset()

                    configureRecorder()

                    val userMessageFormatted = "Orlando" + " said: " + viewModel.unfinishedMessage.content

                    viewModel.addMessage(Message(MESSAGE_ROLE.USER.toString().lowercase(), userMessageFormatted))
                    viewModel.cleanUnfinishedMessage()

                    val lastMessages = viewModel.messages.takeLast(MESSAGES_TO_CHATGPT)
                    val messages = listOf(viewModel.systemMessage) + lastMessages

                    disableButtons(binding)

                    val updateButtons = {
                        binding.recordButton.setEnabled(true)
                        binding.stopButton.setEnabled(false)
                        binding.translateButton.setEnabled(true)
                    }

                    lifecycleScope.launch {
                        val chatCompletionResponse = withContext(Dispatchers.IO) {
                            viewModel.getChatCompletionResponse(ChatCompletionRequestData(
                                messages = messages,
                                temperature = sharedPref.getFloat(context?.getString(R.string.chat_gpt_temperature_key), 0.0f),
                                maxTokens = sharedPref.getInt(context?.getString(R.string.chat_gpt_max_tokens_key), 0),
                                presencePenalty = sharedPref.getFloat(context?.getString(R.string.chat_gpt_presence_penalty_key), 0.0f),
                                frequencyPenalty = sharedPref.getFloat(context?.getString(R.string.chat_gpt_frecuency_penalty_key), 0.0f),
                            ))
                        }

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessage(chatCompletionMessage)

                        addMessageToView(chatCompletionMessage)

                        val textToSpeechResponse = withContext(Dispatchers.IO) {
                            viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(chatCompletionMessage.content), Voice(
                                sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "").toString(),
                                sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "").toString(),
                                sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "").toString()
                            )))
                        }

                        val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                        writeDataToFile(dataDecoded, syntheticCacheFile)

                        configurePlayer()

                        startPlayer()
                        resetUntilFinishedPlaying(updateButtons)
                    }
                    currentRecordingState = RECORDING_STATE.STOP
                }
            }
        }

        binding.translateButton.setOnClickListener {
            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    return@setOnClickListener
                }
                RECORDING_STATE.STOP -> {
                    when (currentTranslatingState) {
                        TRANSLATING_STATE.STOP -> {
                            translator?.start()
                            (binding.translateButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            binding.translateButton.setEnabled(true)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            stopRecordingTranslator()

                            disableButtons(binding)

                            val updateButtons = {
                                (binding.translateButton).icon =
                                    resources.getDrawable(R.drawable.baseline_translate_24, null)
                                binding.recordButton.setEnabled(true)
                                binding.stopButton.setEnabled(false)
                                binding.translateButton.setEnabled(true)
                            }

                            lifecycleScope.launch {
                                val translationResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTranslationResponse(TranslationRequestData(
                                        file = translatingCacheFile,
                                        temperature = sharedPref.getFloat(context?.getString(R.string.whisper_temperature_key), 0.0f)
                                    ))
                                }

                                configureTranslator()

                                val textToSpeechResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(translationResponse.text), Voice(
                                        sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "").toString(),
                                        sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "").toString(),
                                        sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "").toString()
                                    )))
                                }

                                val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                                writeDataToFile(dataDecoded, syntheticCacheFile)

                                configurePlayer()

                                startPlayer()
                                resetUntilFinishedPlaying(updateButtons)
                            }
                            currentTranslatingState = TRANSLATING_STATE.STOP
                        }
                    }
                }
                RECORDING_STATE.PAUSE -> {
                    when (currentTranslatingState) {
                        TRANSLATING_STATE.STOP -> {
                            translator?.start()
                            (binding.translateButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            binding.translateButton.setEnabled(true)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            stopRecordingTranslator()

                            disableButtons(binding)

                            val updateButtons = {
                                (binding.translateButton).icon =
                                    resources.getDrawable(R.drawable.baseline_translate_24, null)
                                binding.recordButton.setEnabled(true)
                                binding.stopButton.setEnabled(true)
                                binding.translateButton.setEnabled(true)
                            }

                            lifecycleScope.launch {
                                val translationResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTranslationResponse(TranslationRequestData(
                                        file = translatingCacheFile,
                                        temperature = sharedPref.getFloat(context?.getString(R.string.whisper_temperature_key), 0.0f)
                                    ))
                                }

                                configureTranslator()

                                val textToSpeechResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(translationResponse.text), Voice(
                                        sharedPref.getString(context?.getString(R.string.text_to_speech_accent_key), "").toString(),
                                        sharedPref.getString(context?.getString(R.string.text_to_speech_voice_name_key), "").toString(),
                                        sharedPref.getString(context?.getString(R.string.text_to_speech_gender_key), "").toString()
                                    )))
                                }

                                val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                                writeDataToFile(dataDecoded, syntheticCacheFile)

                                configurePlayer()

                                startPlayer()
                                resetUntilFinishedPlaying(updateButtons)
                            }
                            currentTranslatingState = TRANSLATING_STATE.STOP
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

        chatLayout = null
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
        try {
            mediaPlayer?.apply {
                setDataSource(syntheticCacheFile.absolutePath)
                prepare()
            }
        } catch (ioException: IOException) {
            val silence = "//NExAARMI4wAEmMTOXAOCYbb/uP3uyYDCwAAAAAAAAAAAAgLJkyZMmnYOAgCAIAgD7xOgkD97vLlwfB8HzmUKOIZfIf/IdZ8uHuwIn7/y/wQTAUQcDAIkVQGiiAiQEB//NExA4QgN5IADDGcFUFDBbeRzk3bXKrkVkM6beFGFWGjIqUAiBK24SQmKpr3sj/ZMR5u9QmQhLUO0IYtM1Xe6kr4jnSPGkfVChlVf2219q0QUbsJrSY4KfFYZ+jT69e//NExB8SQIo0AHjETZt/+32vob37Kt/6m6zeFLP5y325zrH0S2fi60IKxny/9WNyf1bH9/vrpQBQL1mOgeRATYRhIcdh/PdixR6ybj8rtkWbqKZ5J08m0T6JI+gXeuso//NExCkQgDIsAHpGAMG1ANY1zw2oAAakcNALFSCULe8qToGstWgX7QyqBUGILO0cTpIbMjvR2IHlxdYuTcC9j6qDda0oC1IwolTEHiQSceMR6SrJkF2GgG0IjyZXaPDT//NExDoSIDYsAMJSBI4JgKEJ9JggNEw6+17ppQMjR9jQ5ClgiMkuYPUalHpY+ufa5KlDYnuoMKAb2nH0qGvGlNqnLEEWUB2SB9dYfQQNDpAwOScnDBsPpUmxQGoLCr+p//NExEQQ8D4wAErMBAw0Sq9l3VNZvG1ty1WeuT5e0s26U1L/+7NdFl20WyWVf9bWrTKrvR3RohNYfiebKhNwBeD7+Fvym92dD9Xnfhze7utFApoxouTF/iWMy789ds0j//NExFMRAdYsAHhEmT0BPvb+6NRF9i7Wc03vszNKr0Qn1X36JvVexL16Mz3kuQE8HpgKCxEDMJJY2Zxr3k3Biw2QmBPNdSoVwSp57J5aVnjrd/eH+eicX+naRT/NW4LJ//NExGISCfYoAHiEmO7NnZjuIerlu1qSZfuTdkJHBFj9Dy4eOdDPPYz05HcIn0xGNiPVl9yPn/dj/t3/Jerl53i+0/81BWA0HzermRGMkhAQhNk72CaewACNxaV3NEQk//NExGwUKXIkAHjElULdziC2gDMPzHwBCQyOfAPQMh7w/xHmZh/q6cPeH+7p/b4O4YH+jeAOHuD2gDgh3f3////7rdf8voEcQIk11olGTY4iUfQkkmxHRkfQIgsTTIQy//NExG4VMLo8AHmGTUqQpFLKwqJnqilFqopZpETNLIkWqoUOLIWaRImrQodVQocDAQpwwEKoCArAISTAQqhQFaFAVgYUYUMKNVXZijMaqsVVjMahWNVXYUcDATUKAlAE//NExGwhGmYwAGJGuQSgYUdCjYoqEUCjpuCoQ3EXFFZHQhsU0YplagqjjAIEFDAwQcAFjq1llkssqOQkMFBAwQcDW/+wgaqIGIiVVVdNP7CStMMQ//7VVVEXJVVVf/+h//NExDoRgK08ABjGTStNIlb//lVV9IqqqkxBTUUzLjEwMKqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"

            val dataDecoded = decodeBase64ToByteArray(silence)

            writeDataToFile(dataDecoded, syntheticCacheFile)

            mediaPlayer = MediaPlayer()

            mediaPlayer?.apply {
                setDataSource(syntheticCacheFile.absolutePath)
                prepare()
            }
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

    private fun writeDataToFile(data: ByteArray, file: File) {
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.close()
    }

    private fun disableButtons(binding: FragmentConversationBinding) {
        binding.recordButton.setEnabled(false)
        binding.stopButton.setEnabled(false)
        binding.translateButton.setEnabled(false)
    }

    internal fun addMessageToView(message: Message): Int {
        if (message.content == "") {
            return 0
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

        chatLayout?.addView(messageView)

        messageView.id = (1000..9000).random()

        return messageView.id
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
            R.id.menu_settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}