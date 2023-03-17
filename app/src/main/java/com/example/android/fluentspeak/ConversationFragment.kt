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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.fluentspeak.databinding.FragmentConversationBinding
import com.example.android.fluentspeak.network.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.util.*


enum class RECORDING_STATE {
    START, PAUSE, STOP
}

enum class TRANSLATING_STATE {
    START, STOP
}

enum class MESSAGE_ROLE(val value: String) {
    SYSTEM("system"), ASSISTANT("assistant"), USER("user")
}

class ConversationFragment : Fragment() {

    private lateinit var viewModel: ConversationViewModel

    private var currentRecordingState = RECORDING_STATE.STOP
    private var currentTranslatingState = TRANSLATING_STATE.STOP

    private var recorder: MediaRecorder? = null
    private var translator: MediaRecorder? = null

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var recordingCacheFile: File
    private lateinit var translatingCacheFile: File
    private lateinit var syntheticCacheFile: File

    private lateinit var chatLayout: LinearLayout

    private var userMessage: Message = Message(MESSAGE_ROLE.USER.value, "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentConversationBinding.inflate(inflater)

        viewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)

        chatLayout = binding.chatLayout


        val message = Message(MESSAGE_ROLE.SYSTEM.value, "You are a helpful assistant.")

        ConversationData.addMessage(message)

        addMessageView(message)


        binding.recordButton.setOnClickListener {

            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    recorder?.stop()
                    viewModel.getWhisperResponse(
                        WhisperRequestData(
                            file = recordingCacheFile,
                            prompt = userMessage.content
                        )
                    )
                        .observe(viewLifecycleOwner, Observer {
                            configureRecorder()

                            val tempUserMessage = Message(MESSAGE_ROLE.USER.value, it.text.trim())

                            userMessage.content += " " + tempUserMessage.content
                            userMessage.content.trim()

                            addMessageView(tempUserMessage)
                        })
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_play_arrow_24, null)
                    binding.translateButton.setEnabled(true)
                    currentRecordingState = RECORDING_STATE.PAUSE
                }
                RECORDING_STATE.STOP -> {
                    recorder?.start()
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_pause_24, null)
                    binding.stopButton.setEnabled(true)
                    binding.translateButton.setEnabled(false)
                    currentRecordingState = RECORDING_STATE.START
                }
                RECORDING_STATE.PAUSE -> {
                    recorder?.start()
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_pause_24, null)
                    binding.translateButton.setEnabled(false)
                    currentRecordingState = RECORDING_STATE.START
                }

            }
        }

        binding.stopButton.setOnClickListener {
            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    recorder?.stop()

                    viewModel.getWhisperResponse(
                        WhisperRequestData(
                            file = recordingCacheFile,
                            prompt = userMessage.content
                        )
                    )
                        .observe(viewLifecycleOwner, Observer {
                            configureRecorder()

                            val tempUserMessage = Message(MESSAGE_ROLE.USER.value, it.text.trim())

                            userMessage.content += " " + tempUserMessage.content
                            userMessage.content.trim()

                            addMessageView(tempUserMessage)


                            ConversationData.addMessage(
                                Message(
                                    MESSAGE_ROLE.USER.value,
                                    userMessage.content
                                )
                            )
                            userMessage.content = ""

                            val messages = ConversationData.getMessages()

                            viewModel.getChatGPTResponse(ChatGPTRequestData(messages = messages))
                                .observe(viewLifecycleOwner, Observer {
                                    println("Contenido:" + it.choices[0].message.content)

                                    val chatGPTMessage = Message(
                                        MESSAGE_ROLE.ASSISTANT.value,
                                        it.choices[0].message.content
                                    )

                                    ConversationData.addMessage(chatGPTMessage)

                                    addMessageView(chatGPTMessage)

                                    viewModel.getTextToSpeechResponse(
                                        TextToSpeechRequestData(
                                            Input(
                                                chatGPTMessage.content
                                            )
                                        )
                                    )
                                        .observe(viewLifecycleOwner, Observer {
                                            println(it.audioContent)

                                            val dataDecoded: ByteArray =
                                                android.util.Base64.decode(
                                                    it.audioContent,
                                                    android.util.Base64.DEFAULT
                                                )

                                            val fos =
                                                FileOutputStream(syntheticCacheFile)
                                            fos.write(dataDecoded)
                                            fos.close()

                                            configurePlayer()

                                            mediaPlayer?.start()
                                            mediaPlayer?.setOnCompletionListener {
                                                mediaPlayer?.reset()
                                            }
                                        })
                                })

                        })
                    it.setEnabled(false)
                    (binding.recordButton as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_mic_24, null)
                    binding.translateButton.setEnabled(true)
                    currentRecordingState = RECORDING_STATE.STOP
                }
                RECORDING_STATE.STOP -> return@setOnClickListener
                RECORDING_STATE.PAUSE -> {
                    recorder?.reset()

                    configureRecorder()


                    ConversationData.addMessage(
                        Message(
                            MESSAGE_ROLE.USER.value,
                            userMessage.content
                        )
                    )
                    userMessage.content = ""

                    val messages = ConversationData.getMessages()

                    viewModel.getChatGPTResponse(ChatGPTRequestData(messages = messages))
                        .observe(viewLifecycleOwner, Observer {
                            println("Contenido:" + it.choices[0].message.content)

                            val chatGPTMessage = Message(
                                MESSAGE_ROLE.ASSISTANT.value,
                                it.choices[0].message.content
                            )

                            ConversationData.addMessage(chatGPTMessage)

                            addMessageView(chatGPTMessage)

                            viewModel.getTextToSpeechResponse(
                                TextToSpeechRequestData(
                                    Input(
                                        chatGPTMessage.content
                                    )
                                )
                            )
                                .observe(viewLifecycleOwner, Observer {
                                    println(it.audioContent)

                                    val dataDecoded: ByteArray =
                                        android.util.Base64.decode(
                                            it.audioContent,
                                            android.util.Base64.DEFAULT
                                        )

                                    val fos =
                                        FileOutputStream(syntheticCacheFile)
                                    fos.write(dataDecoded)
                                    fos.close()

                                    configurePlayer()

                                    mediaPlayer?.start()
                                    mediaPlayer?.setOnCompletionListener {
                                        mediaPlayer?.reset()
                                    }
                                })
                        })
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
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            translator?.stop()
                            configureTranslator()
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_translate_24, null)
                            binding.recordButton.setEnabled(true)
                            currentTranslatingState = TRANSLATING_STATE.STOP
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
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            translator?.stop()
                            configureTranslator()
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_translate_24, null)
                            binding.recordButton.setEnabled(true)
                            binding.stopButton.setEnabled(true)
                            currentTranslatingState = TRANSLATING_STATE.STOP
                        }
                    }
                }
            }
        }


        return binding.root
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

    private fun addMessageView(message: Message) {
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
            MESSAGE_ROLE.SYSTEM.value -> {
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                messageView.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.round_corner_textview_system
                )
                messageView.setTextColor(Color.WHITE)
            }
            MESSAGE_ROLE.ASSISTANT.value -> {
                layoutParams.gravity = Gravity.START
                messageView.maxWidth = (screenWidth * 0.6).toInt()
                messageView.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.round_corner_textview_assistant
                )
            }
            MESSAGE_ROLE.USER.value -> {
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
    }
}