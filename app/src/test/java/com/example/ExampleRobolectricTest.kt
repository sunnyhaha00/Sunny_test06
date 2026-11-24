package com.example

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.DailyHealthRepository
import com.example.data.MedicineSlot
import com.example.data.VoiceAction
import com.example.data.VoiceCommandParser
import com.example.ui.DailyHealthScreen
import com.example.ui.DailyHealthViewModel
import com.example.ui.theme.SeniorCareTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("건강체크", appName)
    }

    @Test
    fun `voice parser understands Korean medicine and water phrases`() {
        assertEquals(
            listOf(VoiceAction.SetMedicine(MedicineSlot.MORNING, true)),
            VoiceCommandParser.parse("아침 약 먹었어"),
        )
        assertEquals(
            listOf(VoiceAction.SetMedicine(MedicineSlot.LUNCH, false)),
            VoiceCommandParser.parse("점심 약 취소"),
        )
        assertEquals(
            listOf(VoiceAction.AddWater(2)),
            VoiceCommandParser.parse("물 두 잔 마셨어요"),
        )
        assertEquals(
            listOf(VoiceAction.RemoveWater),
            VoiceCommandParser.parse("물 한 잔 빼줘"),
        )
        assertEquals(
            listOf(
                VoiceAction.SetMedicine(MedicineSlot.DINNER, true),
                VoiceAction.AddWater(1),
            ),
            VoiceCommandParser.parse("저녁 약 먹고 물 한 잔 마셨어"),
        )
        assertTrue(VoiceCommandParser.parse("오늘 날씨 어때").isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `repository persists today record and resets at midnight`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefsFile = File(context.filesDir, "test_prefs_${System.nanoTime()}.preferences_pb")
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))
        var currentDay = "2026-11-17"

        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { prefsFile },
        )

        val repo = DailyHealthRepository(dataStore) { currentDay }
        repo.toggleMedicine(MedicineSlot.MORNING)
        repo.addWaterCup()
        repo.addWaterCup()

        val repoAfterRestart = DailyHealthRepository(dataStore) { currentDay }
        val saved = repoAfterRestart.todayRecord.first()
        assertTrue(saved.morningTaken)
        assertFalse(saved.lunchTaken)
        assertEquals(2, saved.waterCups)

        // Simulate midnight rollover
        currentDay = "2026-11-18"
        repoAfterRestart.ensureDateFresh()
        val nextDayRecord = repoAfterRestart.todayRecord.first()
        assertFalse(nextDayRecord.morningTaken)
        assertEquals(0, nextDayRecord.waterCups)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `screen supports voice check, button check, and midnight reset`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefsFile = File(context.filesDir, "test_ui_prefs_${System.nanoTime()}.preferences_pb")
        val scope = TestScope(UnconfinedTestDispatcher())
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { prefsFile },
        )
        var currentDay = "2026-11-17"
        var dateTitle = "2026년 11월 17일"
        val repository = DailyHealthRepository(dataStore) { currentDay }
        val viewModel = DailyHealthViewModel(
            repository = repository,
            dateKeyProvider = { currentDay },
            dateTitleProvider = { dateTitle },
            dayOfWeekProvider = { "화요일" },
        )

        composeRule.setContent {
            SeniorCareTheme {
                val state = viewModel.uiState.collectAsState().value
                DailyHealthScreen(
                    uiState = state,
                    onToggleMedicine = viewModel::onToggleMedicine,
                    onAddWaterCup = viewModel::onAddWaterCup,
                    onRemoveWaterCup = viewModel::onRemoveWaterCup,
                    onVoiceClick = {},
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("today_date_text").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("voice_check_button").assertIsDisplayed()
        composeRule.onNodeWithTag("medicine_button_morning").assertTextContains("말하거나 눌러서 체크", substring = true)

        // Voice check for morning medicine and 2 cups of water
        viewModel.onSpokenResult("아침 약 먹고 물 두 잔 마셨어")
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("medicine_button_morning").assertTextContains("복용 완료", substring = true)
                composeRule.onNodeWithTag("water_count_text").assertTextContains("2잔")
                true
            }.getOrDefault(false)
        }

        // Midnight rollover automatically resets medicine and water count
        currentDay = "2026-11-18"
        dateTitle = "2026년 11월 18일"
        viewModel.onDateOrTimeChanged()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("today_date_text").assertTextContains("2026년 11월 18일")
                composeRule.onNodeWithTag("medicine_button_morning").assertTextContains("말하거나 눌러서 체크", substring = true)
                composeRule.onNodeWithTag("water_count_text").performScrollTo().assertTextContains("0잔")
                true
            }.getOrDefault(false)
        }

        // Touch fallback still works cleanly
        composeRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("water_count_text").assertTextContains("1잔")
                true
            }.getOrDefault(false)
        }
    }
}
