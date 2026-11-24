package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MedicineSlot {
    MORNING,
    LUNCH,
    DINNER,
}

data class DailyHealthRecord(
    val dateKey: String,
    val morningTaken: Boolean = false,
    val lunchTaken: Boolean = false,
    val dinnerTaken: Boolean = false,
    val waterCups: Int = 0,
) {
    val takenMedicineCount: Int
        get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }
}

object KoreanDateFormatter {
    fun todayDateKey(date: Date = Date()): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return formatter.format(date)
    }

    fun formatDateTitle(date: Date = Date()): String {
        val formatter = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
        return formatter.format(date)
    }

    fun formatDayOfWeek(date: Date = Date()): String {
        val formatter = SimpleDateFormat("EEEE", Locale.KOREA)
        return formatter.format(date)
    }

    fun millisUntilNextMidnight(nowMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 100)
        }
        return (calendar.timeInMillis - nowMillis).coerceAtLeast(1_000L)
    }
}
