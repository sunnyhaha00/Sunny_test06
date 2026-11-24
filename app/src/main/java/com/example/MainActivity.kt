package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DailyHealthRepository
import com.example.ui.DailyHealthScreen
import com.example.ui.DailyHealthUiState
import com.example.ui.DailyHealthViewModel
import com.example.ui.rememberVoiceCheckHandle
import com.example.ui.theme.SeniorCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = DailyHealthRepository(applicationContext)

        setContent {
            SeniorCareTheme {
                val viewModel: DailyHealthViewModel = viewModel(
                    factory = DailyHealthViewModel.factory(repository)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                // Check date rollover whenever returning to foreground or when system date/time changes
                DisposableEffect(lifecycleOwner, viewModel) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.onDateOrTimeChanged()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    val dateChangeReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            viewModel.onDateOrTimeChanged()
                        }
                    }
                    val filter = IntentFilter().apply {
                        addAction(Intent.ACTION_DATE_CHANGED)
                        addAction(Intent.ACTION_TIME_CHANGED)
                        addAction(Intent.ACTION_TIMEZONE_CHANGED)
                    }
                    ContextCompat.registerReceiver(
                        this@MainActivity,
                        dateChangeReceiver,
                        filter,
                        ContextCompat.RECEIVER_NOT_EXPORTED,
                    )

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        unregisterReceiver(dateChangeReceiver)
                    }
                }

                val isListening = (uiState as? DailyHealthUiState.Ready)?.voiceState?.isListening == true
                val voiceHandle = rememberVoiceCheckHandle(
                    isListening = isListening,
                    onListeningChanged = viewModel::setListening,
                    onSpokenResult = viewModel::onSpokenResult,
                    onPermissionDenied = viewModel::onVoicePermissionDenied,
                    onSpeechUnavailable = viewModel::onSpeechUnavailable,
                    onSpeechError = viewModel::onSpeechError,
                )

                DailyHealthScreen(
                    uiState = uiState,
                    onToggleMedicine = viewModel::onToggleMedicine,
                    onAddWaterCup = viewModel::onAddWaterCup,
                    onRemoveWaterCup = viewModel::onRemoveWaterCup,
                    onVoiceClick = voiceHandle.onVoiceButtonClick,
                )
            }
        }
    }
}
