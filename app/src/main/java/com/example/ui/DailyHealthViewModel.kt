package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyHealthRecord
import com.example.data.DailyHealthRepository
import com.example.data.KoreanDateFormatter
import com.example.data.MedicineSlot
import com.example.data.VoiceAction
import com.example.data.VoiceCommandParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class VoiceUiState(
    val isListening: Boolean = false,
    val lastSpokenText: String? = null,
    val feedbackMessage: String? = null,
    val isFeedbackError: Boolean = false,
    val isPermissionDenied: Boolean = false,
    val isSpeechUnavailable: Boolean = false,
)

sealed interface DailyHealthUiState {
    data object Loading : DailyHealthUiState

    data class Ready(
        val dateTitle: String,
        val dayOfWeek: String,
        val record: DailyHealthRecord,
        val voiceState: VoiceUiState = VoiceUiState(),
        val waterGoalCups: Int = 8,
    ) : DailyHealthUiState
}

class DailyHealthViewModel(
    private val repository: DailyHealthRepository,
    private val dateKeyProvider: () -> String = { KoreanDateFormatter.todayDateKey() },
    private val dateTitleProvider: () -> String = { KoreanDateFormatter.formatDateTitle() },
    private val dayOfWeekProvider: () -> String = { KoreanDateFormatter.formatDayOfWeek() },
) : ViewModel() {

    private val currentDateKey = MutableStateFlow(dateKeyProvider())
    private val voiceState = MutableStateFlow(VoiceUiState())

    val uiState: StateFlow<DailyHealthUiState> = combine(
        repository.observeRecord(currentDateKey),
        voiceState,
    ) { record, voice ->
        DailyHealthUiState.Ready(
            dateTitle = dateTitleProvider(),
            dayOfWeek = dayOfWeekProvider(),
            record = record,
            voiceState = voice,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DailyHealthUiState.Loading,
    )

    init {
        viewModelScope.launch {
            repository.ensureDateFresh()
        }
        startMidnightWatcher()
    }

    private fun startMidnightWatcher() {
        viewModelScope.launch {
            while (isActive) {
                val waitMillis = KoreanDateFormatter.millisUntilNextMidnight()
                delay(waitMillis)
                onDateOrTimeChanged()
            }
        }
    }

    fun onDateOrTimeChanged() {
        val today = dateKeyProvider()
        val previous = currentDateKey.value
        currentDateKey.value = today
        viewModelScope.launch {
            repository.ensureDateFresh()
            if (previous != today) {
                voiceState.update {
                    it.copy(
                        lastSpokenText = null,
                        feedbackMessage = "자정이 지나 오늘 기록이 새로 시작되었어요.",
                        isFeedbackError = false,
                    )
                }
            }
        }
    }

    fun onToggleMedicine(slot: MedicineSlot) {
        viewModelScope.launch {
            repository.toggleMedicine(slot)
        }
    }

    fun onAddWaterCup() {
        viewModelScope.launch {
            repository.addWaterCup()
        }
    }

    fun onRemoveWaterCup() {
        viewModelScope.launch {
            repository.removeWaterCup()
        }
    }

    fun setListening(listening: Boolean) {
        voiceState.update {
            it.copy(
                isListening = listening,
                isPermissionDenied = false,
                isSpeechUnavailable = false,
                feedbackMessage = if (listening) null else it.feedbackMessage,
                isFeedbackError = if (listening) false else it.isFeedbackError,
            )
        }
    }

    fun onVoicePermissionDenied() {
        voiceState.update {
            it.copy(
                isListening = false,
                isPermissionDenied = true,
                feedbackMessage = "마이크 권한을 허용하시면 말로 체크할 수 있어요.",
                isFeedbackError = true,
            )
        }
    }

    fun onSpeechUnavailable() {
        voiceState.update {
            it.copy(
                isListening = false,
                isSpeechUnavailable = true,
                feedbackMessage = "이 기기에서는 음성 인식을 사용할 수 없어요. 버튼을 눌러 체크해 주세요.",
                isFeedbackError = true,
            )
        }
    }

    fun onSpeechError(message: String) {
        voiceState.update {
            it.copy(
                isListening = false,
                feedbackMessage = message,
                isFeedbackError = true,
            )
        }
    }

    fun onSpokenResult(spokenText: String): String {
        val actions = VoiceCommandParser.parse(spokenText)
        if (actions.isEmpty()) {
            val helpMessage = "잘 못 들었어요. “아침 약”이나 “물 한 잔”이라고 말씀해 주세요."
            voiceState.update {
                it.copy(
                    isListening = false,
                    lastSpokenText = spokenText,
                    feedbackMessage = helpMessage,
                    isFeedbackError = true,
                )
            }
            return helpMessage
        }

        viewModelScope.launch {
            actions.forEach { action ->
                when (action) {
                    is VoiceAction.SetMedicine -> repository.setMedicineTaken(action.slot, action.taken)
                    VoiceAction.MarkAllMedicines -> repository.markAllMedicinesTaken()
                    is VoiceAction.AddWater -> repository.addWaterCups(action.cups)
                    VoiceAction.RemoveWater -> repository.removeWaterCup()
                }
            }
        }

        val confirmation = buildConfirmationMessage(actions)
        voiceState.update {
            it.copy(
                isListening = false,
                lastSpokenText = spokenText,
                feedbackMessage = confirmation,
                isFeedbackError = false,
            )
        }
        return confirmation
    }

    private fun buildConfirmationMessage(actions: List<VoiceAction>): String {
        val parts = actions.map { action ->
            when (action) {
                is VoiceAction.SetMedicine -> {
                    val name = when (action.slot) {
                        MedicineSlot.MORNING -> "아침 약"
                        MedicineSlot.LUNCH -> "점심 약"
                        MedicineSlot.DINNER -> "저녁 약"
                    }
                    if (action.taken) "$name 복용 완료" else "$name 체크 취소"
                }
                VoiceAction.MarkAllMedicines -> "오늘 약 모두 복용 완료"
                is VoiceAction.AddWater -> "물 ${action.cups}잔 추가"
                VoiceAction.RemoveWater -> "물 1잔 빼기"
            }
        }
        return parts.joinToString(", ") + "로 체크했어요."
    }

    companion object {
        fun factory(repository: DailyHealthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DailyHealthViewModel(repository) as T
                }
            }
    }
}
