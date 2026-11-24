package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private val Context.healthDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_health_prefs"
)

class DailyHealthRepository(
    private val dataStore: DataStore<Preferences>,
    private val dateKeyProvider: () -> String = { KoreanDateFormatter.todayDateKey() },
) {
    constructor(
        context: Context,
        dateKeyProvider: () -> String = { KoreanDateFormatter.todayDateKey() },
    ) : this(context.applicationContext.healthDataStore, dateKeyProvider)

    private object Keys {
        val DATE_KEY = stringPreferencesKey("saved_date_key")
        val MORNING_TAKEN = booleanPreferencesKey("morning_taken")
        val LUNCH_TAKEN = booleanPreferencesKey("lunch_taken")
        val DINNER_TAKEN = booleanPreferencesKey("dinner_taken")
        val WATER_CUPS = intPreferencesKey("water_cups")
    }

    val todayRecord: Flow<DailyHealthRecord> = dataStore.data.combine(
        kotlinx.coroutines.flow.flow {
            emit(dateKeyProvider())
        }
    ) { prefs, _ ->
        mapPreferences(prefs, dateKeyProvider())
    }

    fun observeRecord(dateKeyFlow: Flow<String>): Flow<DailyHealthRecord> =
        combine(dataStore.data, dateKeyFlow) { prefs, currentDate ->
            mapPreferences(prefs, currentDate)
        }

    private fun mapPreferences(prefs: Preferences, today: String): DailyHealthRecord {
        val savedDate = prefs[Keys.DATE_KEY]
        return if (savedDate == today) {
            DailyHealthRecord(
                dateKey = today,
                morningTaken = prefs[Keys.MORNING_TAKEN] ?: false,
                lunchTaken = prefs[Keys.LUNCH_TAKEN] ?: false,
                dinnerTaken = prefs[Keys.DINNER_TAKEN] ?: false,
                waterCups = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0),
            )
        } else {
            DailyHealthRecord(dateKey = today)
        }
    }

    suspend fun ensureDateFresh() {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
        }
    }

    suspend fun toggleMedicine(slot: MedicineSlot) {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val key = keyForSlot(slot)
            val current = prefs[key] ?: false
            prefs[key] = !current
        }
    }

    suspend fun setMedicineTaken(slot: MedicineSlot, taken: Boolean) {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            prefs[keyForSlot(slot)] = taken
        }
    }

    suspend fun markAllMedicinesTaken() {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            prefs[Keys.MORNING_TAKEN] = true
            prefs[Keys.LUNCH_TAKEN] = true
            prefs[Keys.DINNER_TAKEN] = true
        }
    }

    suspend fun addWaterCup() {
        addWaterCups(1)
    }

    suspend fun addWaterCups(count: Int) {
        if (count <= 0) return
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val current = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0)
            prefs[Keys.WATER_CUPS] = current + count
        }
    }

    suspend fun removeWaterCup() {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val current = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0)
            if (current > 0) {
                prefs[Keys.WATER_CUPS] = current - 1
            }
        }
    }

    private fun keyForSlot(slot: MedicineSlot) = when (slot) {
        MedicineSlot.MORNING -> Keys.MORNING_TAKEN
        MedicineSlot.LUNCH -> Keys.LUNCH_TAKEN
        MedicineSlot.DINNER -> Keys.DINNER_TAKEN
    }

    private fun resetIfNewDay(prefs: MutablePreferences, today: String) {
        if (prefs[Keys.DATE_KEY] != today) {
            prefs[Keys.DATE_KEY] = today
            prefs[Keys.MORNING_TAKEN] = false
            prefs[Keys.LUNCH_TAKEN] = false
            prefs[Keys.DINNER_TAKEN] = false
            prefs[Keys.WATER_CUPS] = 0
        }
    }
}
