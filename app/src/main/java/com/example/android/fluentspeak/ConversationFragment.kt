package com.example.android.fluentspeak

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.fluentspeak.databinding.FragmentConversationBinding
import com.example.android.fluentspeak.network.ChatGPTRequestData
import com.example.android.fluentspeak.network.Message
import com.example.android.fluentspeak.network.WhisperRequestData
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

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

    private lateinit var recorder: MediaRecorder
    private lateinit var translator: MediaRecorder

    private lateinit var recordingCacheFile: File
    private lateinit var translatingCacheFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentConversationBinding.inflate(inflater)

        viewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)

        ConversationData.addMessage(Message(MESSAGE_ROLE.SYSTEM.value, "You are a helpful assistant."))

        binding.recordButton.setOnClickListener {
            when (currentRecordingState) {
                RECORDING_STATE.START -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        recorder.pause()
                    }
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_play_arrow_24, null)
                    binding.translateButton.setEnabled(true)
                    currentRecordingState = RECORDING_STATE.PAUSE
                }
                RECORDING_STATE.STOP -> {
                    recorder.start()
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_pause_24, null)
                    binding.stopButton.setEnabled(true)
                    binding.translateButton.setEnabled(false)
                    currentRecordingState = RECORDING_STATE.START
                }
                RECORDING_STATE.PAUSE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        recorder.resume()
                    }
                    (it as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_pause_24, null)
                    binding.translateButton.setEnabled(false)
                    currentRecordingState = RECORDING_STATE.START
                }
            }
        }

        binding.stopButton.setOnClickListener {
            when (currentRecordingState) {
                RECORDING_STATE.START, RECORDING_STATE.PAUSE -> {
                    recorder.stop()
                    viewModel.getWhisperResponse(WhisperRequestData(file = recordingCacheFile)).observe(viewLifecycleOwner, Observer {

                        println("Whisper:" + it.text)

                        ConversationData.addMessage(Message(MESSAGE_ROLE.USER.value, it.text))
                        val messages = ConversationData.getMessages()

                        viewModel.getChatGPTResponse(ChatGPTRequestData(messages = messages)).observe(viewLifecycleOwner, Observer {
                            println("Contenido:" + it.choices[0].message.content)
                        })
                        configureRecorder()
                    })
                    it.setEnabled(false)
                    (binding.recordButton as MaterialButton).icon =
                        resources.getDrawable(R.drawable.baseline_mic_24, null)
                    binding.translateButton.setEnabled(true)
                    currentRecordingState = RECORDING_STATE.STOP
                }
                RECORDING_STATE.STOP -> return@setOnClickListener
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
                            translator.start()
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            translator.stop()
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
                            translator.start()
                            (binding.translateButton as MaterialButton).icon =
                                resources.getDrawable(R.drawable.baseline_stop_24, null)
                            binding.recordButton.setEnabled(false)
                            binding.stopButton.setEnabled(false)
                            currentTranslatingState = TRANSLATING_STATE.START
                        }
                        TRANSLATING_STATE.START -> {
                            translator.stop()
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
        recorder.release()
        translator.release()

        recordingCacheFile.delete()
        translatingCacheFile.delete()
    }

    private fun configureRecorder() {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            File.createTempFile("recording.m4a", null, context?.cacheDir)
            recordingCacheFile = File(context?.cacheDir, "recording.m4a")

            setOutputFile(recordingCacheFile.absolutePath)

            prepare()
        }
    }

    private fun configureTranslator() {
        translator.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            File.createTempFile("translation.m4a", null, context?.cacheDir)
            translatingCacheFile = File(context?.cacheDir, "translation.m4a")

            setOutputFile(translatingCacheFile.absolutePath)

            prepare()
        }
    }
}