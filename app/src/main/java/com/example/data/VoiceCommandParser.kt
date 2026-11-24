package com.example.data

sealed interface VoiceAction {
    data class SetMedicine(val slot: MedicineSlot, val taken: Boolean) : VoiceAction
    data object MarkAllMedicines : VoiceAction
    data class AddWater(val cups: Int) : VoiceAction
    data object RemoveWater : VoiceAction
}

object VoiceCommandParser {

    private val cancelKeywords = listOf(
        "취소",
        "안 먹",
        "안먹",
        "안 마",
        "안마",
        "빼",
        "지워",
        "되돌",
        "삭제",
    )

    fun parse(spokenText: String): List<VoiceAction> {
        val normalized = spokenText.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()

        val actions = mutableListOf<VoiceAction>()
        val hasCancelIntent = cancelKeywords.any { normalized.contains(it) }

        val mentionsMorning = normalized.contains("아침") || normalized.contains("조식")
        val mentionsLunch = normalized.contains("점심") || normalized.contains("중식")
        val mentionsDinner = normalized.contains("저녁") || normalized.contains("석식") || normalized.contains("밤")
        val mentionsAllMedicine = (normalized.contains("모두") || normalized.contains("다 먹") || normalized.contains("다먹") || normalized.contains("전부")) &&
            normalized.contains("약")

        if (mentionsAllMedicine && !hasCancelIntent) {
            actions += VoiceAction.MarkAllMedicines
        } else {
            if (mentionsMorning) {
                actions += VoiceAction.SetMedicine(MedicineSlot.MORNING, !hasCancelIntent)
            }
            if (mentionsLunch) {
                actions += VoiceAction.SetMedicine(MedicineSlot.LUNCH, !hasCancelIntent)
            }
            if (mentionsDinner) {
                actions += VoiceAction.SetMedicine(MedicineSlot.DINNER, !hasCancelIntent)
            }
        }

        val mentionsWater = normalized.contains("물") ||
            normalized.contains("한 잔") ||
            normalized.contains("한잔") ||
            normalized.contains("두 잔") ||
            normalized.contains("두잔") ||
            normalized.contains("세 잔") ||
            normalized.contains("세잔") ||
            normalized.contains("한 컵") ||
            normalized.contains("한컵") ||
            normalized.contains("컵") ||
            (normalized.contains("마셨") && !mentionsMorning && !mentionsLunch && !mentionsDinner) ||
            (normalized.contains("마신") && !mentionsMorning && !mentionsLunch && !mentionsDinner)

        if (mentionsWater) {
            val waterCanceled = normalized.contains("빼") ||
                normalized.contains("되돌") ||
                (hasCancelIntent && !mentionsMorning && !mentionsLunch && !mentionsDinner) ||
                normalized.contains("물 취소") ||
                normalized.contains("물취소")
            if (waterCanceled) {
                actions += VoiceAction.RemoveWater
            } else {
                actions += VoiceAction.AddWater(extractCupCount(normalized))
            }
        }

        return actions
    }

    private fun extractCupCount(text: String): Int {
        return when {
            text.contains("다섯") || text.contains("5잔") || text.contains("5컵") -> 5
            text.contains("네 잔") || text.contains("네잔") || text.contains("4잔") || text.contains("4컵") -> 4
            text.contains("세 잔") || text.contains("세잔") || text.contains("3잔") || text.contains("3컵") -> 3
            text.contains("두 잔") || text.contains("두잔") || text.contains("2잔") || text.contains("2컵") -> 2
            else -> 1
        }
    }
}
