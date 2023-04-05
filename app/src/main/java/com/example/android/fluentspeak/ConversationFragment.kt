package com.example.android.fluentspeak

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.android.fluentspeak.databinding.FragmentConversationBinding
import com.example.android.fluentspeak.network.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*


enum class RECORDING_STATE {
    START, PAUSE, STOP
}

enum class TRANSLATING_STATE {
    START, STOP
}

enum class MESSAGE_ROLE {
    SYSTEM, ASSISTANT, USER
}

class ConversationFragment : Fragment() {

    private var currentRecordingState = RECORDING_STATE.STOP
    private var currentTranslatingState = TRANSLATING_STATE.STOP

    private var recorder: MediaRecorder? = null
    private var translator: MediaRecorder? = null

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var recordingCacheFile: File
    private lateinit var translatingCacheFile: File
    private lateinit var syntheticCacheFile: File

    private lateinit var chatLayout: LinearLayout

    private val viewModel: ConversationViewModel by viewModels<ConversationViewModel> {
        ConversationViewModelFactory((requireContext().applicationContext as FluentSpeakApplication).apisRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentConversationBinding.inflate(inflater)

        chatLayout = binding.chatLayout


        val message = Message(MESSAGE_ROLE.SYSTEM.toString().lowercase(), "You are a helpful assistant.")

        viewModel.addMessageToConversationData(message)

        addMessageToView(message)

        setupListeners(binding)

        return binding.root
    }

    fun setupListeners(binding: FragmentConversationBinding) {
        binding.recordButton.setOnClickListener {

            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    recorder?.stop()

                    disableButtons(binding)

                    val updateButtons = {
                        (it as MaterialButton).icon =
                            resources.getDrawable(R.drawable.baseline_play_arrow_24, null)
                        binding.recordButton.setEnabled(true)
                        binding.stopButton.setEnabled(true)
                        binding.translateButton.setEnabled(true)
                        currentRecordingState = RECORDING_STATE.PAUSE
                    }

                    lifecycleScope.launch {
                        val transcriptionResponse = withContext(Dispatchers.IO) {
                            viewModel.getTranscriptionResponse(TranscriptionRequestData(file = recordingCacheFile, prompt = viewModel.unfinishedUserMessage.content))
                        }

                        configureRecorder()

                        val userMessagePortion = Message(MESSAGE_ROLE.USER.toString().lowercase(), transcriptionResponse.text.trim())

                        viewModel.addMessageToUnfinishedUserMessage(userMessagePortion)

                        addMessageToView(userMessagePortion)

                        updateButtons()
                    }
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
                    recorder?.stop()

                    disableButtons(binding)

                    val updateButtons = {
                        (binding.recordButton as MaterialButton).icon =
                            resources.getDrawable(R.drawable.baseline_mic_24, null)
                        binding.recordButton.setEnabled(true)
                        binding.stopButton.setEnabled(false)
                        binding.translateButton.setEnabled(true)
                        currentRecordingState = RECORDING_STATE.STOP
                    }

                    lifecycleScope.launch {
                        val transcriptionResponse = withContext(Dispatchers.IO) {
                            viewModel.getTranscriptionResponse(TranscriptionRequestData(file = recordingCacheFile, prompt = viewModel.unfinishedUserMessage.content))
                        }

                        configureRecorder()

                        val userMessagePortion = Message(MESSAGE_ROLE.USER.toString().lowercase(), transcriptionResponse.text.trim())

                        viewModel.addMessageToUnfinishedUserMessage(userMessagePortion)

                        addMessageToView(userMessagePortion)

                        viewModel.addMessageToConversationData(Message(MESSAGE_ROLE.USER.toString().lowercase(), viewModel.unfinishedUserMessage.content))
                        cleanUnfinishedUserMessage()

                        val messages = ConversationData.messages

                        val chatCompletionResponse = withContext(Dispatchers.IO) {
                            viewModel.getChatCompletionResponse(ChatCompletionRequestData(messages = messages))
                        }

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessageToConversationData(chatCompletionMessage)

                        addMessageToView(chatCompletionMessage)

                        val textToSpeechResponse = withContext(Dispatchers.IO) {
                            viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(chatCompletionMessage.content)))
                        }

                        val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                        writeDataToFile(dataDecoded, syntheticCacheFile)

                        configurePlayer()

                        startPlayer()
                        resetUntilFinishedPlaying(updateButtons)
                    }
                }
                RECORDING_STATE.STOP -> return@setOnClickListener
                RECORDING_STATE.PAUSE -> {
                    recorder?.reset()

                    configureRecorder()

                    viewModel.addMessageToConversationData(Message(MESSAGE_ROLE.USER.toString().lowercase(), viewModel.unfinishedUserMessage.content))
                    cleanUnfinishedUserMessage()

                    val messages = ConversationData.messages

                    disableButtons(binding)

                    val updateButtons = {
                        binding.recordButton.setEnabled(true)
                        binding.stopButton.setEnabled(false)
                        binding.translateButton.setEnabled(true)
                        currentRecordingState = RECORDING_STATE.STOP
                    }

                    lifecycleScope.launch {
                        val chatCompletionResponse = withContext(Dispatchers.IO) {
                            viewModel.getChatCompletionResponse(ChatCompletionRequestData(messages = messages))
                        }

                        val chatCompletionMessage = Message(
                            MESSAGE_ROLE.ASSISTANT.toString().lowercase(),
                            chatCompletionResponse.choices[0].message.content
                        )

                        viewModel.addMessageToConversationData(chatCompletionMessage)

                        addMessageToView(chatCompletionMessage)

                        val textToSpeechResponse = withContext(Dispatchers.IO) {
                            viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(chatCompletionMessage.content)))
                        }

                        val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                        writeDataToFile(dataDecoded, syntheticCacheFile)

                        configurePlayer()

                        startPlayer()
                        resetUntilFinishedPlaying(updateButtons)
                    }
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
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            binding.translateButton.setEnabled(true)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            translator?.stop()

                            disableButtons(binding)

                            val updateButtons = {
                                (binding.translateButton as MaterialButton).icon =
                                    resources.getDrawable(R.drawable.baseline_translate_24, null)
                                binding.recordButton.setEnabled(true)
                                binding.stopButton.setEnabled(false)
                                binding.translateButton.setEnabled(true)
                                currentTranslatingState = TRANSLATING_STATE.STOP
                            }

                            lifecycleScope.launch {
                                val translationResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTranslationResponse(TranslationRequestData(file = translatingCacheFile))
                                }

                                configureTranslator()

                                val textToSpeechResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(translationResponse.text)))
                                }

                                val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                                writeDataToFile(dataDecoded, syntheticCacheFile)

                                configurePlayer()

                                startPlayer()
                                resetUntilFinishedPlaying(updateButtons)
                            }
                        }
                    }
                }
                RECORDING_STATE.PAUSE -> {
                    when (currentTranslatingState) {
                        TRANSLATING_STATE.STOP -> {
                            translator?.start()
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            binding.translateButton.setEnabled(true)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            translator?.stop()

                            disableButtons(binding)

                            val updateButtons = {
                                (binding.translateButton as MaterialButton).icon =
                                    resources.getDrawable(R.drawable.baseline_translate_24, null)
                                binding.recordButton.setEnabled(true)
                                binding.stopButton.setEnabled(true)
                                binding.translateButton.setEnabled(true)
                                currentTranslatingState = TRANSLATING_STATE.STOP
                            }

                            lifecycleScope.launch {
                                val translationResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTranslationResponse(TranslationRequestData(file = translatingCacheFile))
                                }

                                configureTranslator()

                                val textToSpeechResponse = withContext(Dispatchers.IO) {
                                    viewModel.getTextToSpeechResponse(TextToSpeechRequestData(Input(translationResponse.text)))
                                }

                                val dataDecoded = decodeBase64ToByteArray(textToSpeechResponse.audioContent)

                                writeDataToFile(dataDecoded, syntheticCacheFile)

                                configurePlayer()

                                startPlayer()
                                resetUntilFinishedPlaying(updateButtons)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        recorder = MediaRecorder(requireContext())
                        translator = MediaRecorder(requireContext())
                    } else {
                        recorder = MediaRecorder()
                        translator = MediaRecorder()
                    }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    recorder = MediaRecorder(requireContext())
                    translator = MediaRecorder(requireContext())
                } else {
                    recorder = MediaRecorder()
                    translator = MediaRecorder()
                }

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

    private fun configurePlayer() {
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

    private fun cleanUnfinishedUserMessage() {
        viewModel.unfinishedUserMessage.content = ""
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

        chatLayout.addView(messageView)

        messageView.id = (1000..9000).random()

        return messageView.id
    }

    private fun decodeBase64ToByteArray(encodedBase64: String): ByteArray {
        return android.util.Base64.decode(encodedBase64, android.util.Base64.DEFAULT)
    }
}