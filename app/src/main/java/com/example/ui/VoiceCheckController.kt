package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

class VoiceCheckHandle(
    val onVoiceButtonClick: () -> Unit,
)

@Composable
fun rememberVoiceCheckHandle(
    isListening: Boolean,
    onListeningChanged: (Boolean) -> Unit,
    onSpokenResult: (String) -> String,
    onPermissionDenied: () -> Unit,
    onSpeechUnavailable: () -> Unit,
    onSpeechError: (String) -> Unit,
): VoiceCheckHandle {
    val context = LocalContext.current
    val currentIsListening by rememberUpdatedState(isListening)
    val currentOnListeningChanged by rememberUpdatedState(onListeningChanged)
    val currentOnSpokenResult by rememberUpdatedState(onSpokenResult)
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)
    val currentOnSpeechUnavailable by rememberUpdatedState(onSpeechUnavailable)
    val currentOnSpeechError by rememberUpdatedState(onSpeechError)

    var ttsReady by remember { mutableStateOf(false) }
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        tts.language = Locale.KOREAN
        textToSpeech = tts
        onDispose {
            ttsReady = false
            tts.stop()
            tts.shutdown()
            textToSpeech = null
        }
    }

    val speakConfirmation: (String) -> Unit = remember(context) {
        { message ->
            if (ttsReady) {
                textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "voice_confirm")
            }
        }
    }

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                currentOnListeningChanged(true)
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                currentOnListeningChanged(false)
            }

            override fun onError(error: Int) {
                currentOnListeningChanged(false)
                when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> currentOnPermissionDenied()
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        val msg = "소리가 잘 안 들렸어요. 다시 눌러서 말씀해 주세요."
                        currentOnSpeechError(msg)
                        speakConfirmation(msg)
                    }
                    SpeechRecognizer.ERROR_CLIENT -> Unit
                    else -> {
                        val msg = "음성 인식이 잠시 원활하지 않아요. 다시 시도해 주세요."
                        currentOnSpeechError(msg)
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                currentOnListeningChanged(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull().orEmpty()
                if (spoken.isBlank()) {
                    val msg = "소리가 잘 안 들렸어요. 다시 눌러서 말씀해 주세요."
                    currentOnSpeechError(msg)
                    speakConfirmation(msg)
                } else {
                    val confirmation = currentOnSpokenResult(spoken)
                    speakConfirmation(confirmation)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val activitySpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        currentOnListeningChanged(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                val confirmation = currentOnSpokenResult(spoken)
                speakConfirmation(confirmation)
            } else {
                currentOnSpeechError("소리가 잘 안 들렸어요. 다시 눌러서 말씀해 주세요.")
            }
        }
    }

    val startRecognition: () -> Unit = remember(speechRecognizer, context) {
        {
            val intent = createKoreanSpeechIntent(context)
            if (speechRecognizer != null) {
                currentOnListeningChanged(true)
                speechRecognizer.startListening(intent)
            } else if (intent.resolveActivity(context.packageManager) != null) {
                currentOnListeningChanged(true)
                activitySpeechLauncher.launch(intent)
            } else {
                currentOnSpeechUnavailable()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecognition()
        } else {
            currentOnPermissionDenied()
        }
    }

    return remember(speechRecognizer) {
        VoiceCheckHandle(
            onVoiceButtonClick = {
                if (currentIsListening) {
                    speechRecognizer?.stopListening()
                    currentOnListeningChanged(false)
                } else {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        startRecognition()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        )
    }
}

private fun createKoreanSpeechIntent(context: Context): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko-KR")
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "“아침 약 먹었어” 또는 “물 한 잔”이라고 말씀하세요")
    }
}
