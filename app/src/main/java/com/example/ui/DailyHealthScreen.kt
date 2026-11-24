package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MedicineSlot
import com.example.ui.theme.DinnerIndigo
import com.example.ui.theme.DinnerIndigoBg
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvoryBackgroundBottom
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.LunchOrange
import com.example.ui.theme.LunchOrangeBg
import com.example.ui.theme.MedicineGreen
import com.example.ui.theme.MedicineGreenBorder
import com.example.ui.theme.MedicineGreenDarkText
import com.example.ui.theme.MedicineGreenSoft
import com.example.ui.theme.MorningAmber
import com.example.ui.theme.MorningAmberBg
import com.example.ui.theme.VoiceActionPrimary
import com.example.ui.theme.VoiceCardBorder
import com.example.ui.theme.VoiceCardSurface
import com.example.ui.theme.VoiceFeedbackErrorBg
import com.example.ui.theme.VoiceFeedbackErrorText
import com.example.ui.theme.VoiceListeningRed
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueBright
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterCardBorder
import com.example.ui.theme.WaterCardSurface
import com.example.ui.theme.WaterCupEmpty
import com.example.ui.theme.WaterCupOutline

@Composable
fun DailyHealthScreen(
    uiState: DailyHealthUiState,
    onToggleMedicine: (MedicineSlot) -> Unit,
    onAddWaterCup: () -> Unit,
    onRemoveWaterCup: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IvoryBackground, IvoryBackgroundBottom)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (uiState) {
            DailyHealthUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MedicineGreen,
                        strokeWidth = 5.dp,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }

            is DailyHealthUiState.Ready -> {
                DailyHealthContent(
                    state = uiState,
                    onToggleMedicine = onToggleMedicine,
                    onAddWaterCup = onAddWaterCup,
                    onRemoveWaterCup = onRemoveWaterCup,
                    onVoiceClick = onVoiceClick,
                )
            }
        }
    }
}

@Composable
private fun DailyHealthContent(
    state: DailyHealthUiState.Ready,
    onToggleMedicine: (MedicineSlot) -> Unit,
    onAddWaterCup: () -> Unit,
    onRemoveWaterCup: () -> Unit,
    onVoiceClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        val isExpanded = maxWidth >= 760.dp

        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    DateHeaderCard(
                        dateTitle = state.dateTitle,
                        dayOfWeek = state.dayOfWeek,
                        takenCount = state.record.takenMedicineCount,
                    )
                    VoiceCheckCard(
                        voiceState = state.voiceState,
                        onVoiceClick = onVoiceClick,
                    )
                    MedicineChecklistSection(
                        morningTaken = state.record.morningTaken,
                        lunchTaken = state.record.lunchTaken,
                        dinnerTaken = state.record.dinnerTaken,
                        onToggleMedicine = onToggleMedicine,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    WaterTrackerCard(
                        waterCups = state.record.waterCups,
                        goalCups = state.waterGoalCups,
                        onAddWaterCup = onAddWaterCup,
                        onRemoveWaterCup = onRemoveWaterCup,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                DateHeaderCard(
                    dateTitle = state.dateTitle,
                    dayOfWeek = state.dayOfWeek,
                    takenCount = state.record.takenMedicineCount,
                )

                VoiceCheckCard(
                    voiceState = state.voiceState,
                    onVoiceClick = onVoiceClick,
                )

                MedicineChecklistSection(
                    morningTaken = state.record.morningTaken,
                    lunchTaken = state.record.lunchTaken,
                    dinnerTaken = state.record.dinnerTaken,
                    onToggleMedicine = onToggleMedicine,
                )

                WaterTrackerCard(
                    waterCups = state.record.waterCups,
                    goalCups = state.waterGoalCups,
                    onAddWaterCup = onAddWaterCup,
                    onRemoveWaterCup = onRemoveWaterCup,
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun VoiceCheckCard(
    voiceState: VoiceUiState,
    onVoiceClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState.isListening) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mic_pulse_scale",
    )

    val buttonColor by animateColorAsState(
        targetValue = if (voiceState.isListening) VoiceListeningRed else VoiceActionPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "voice_btn_color",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_check_card"),
        shape = RoundedCornerShape(24.dp),
        color = VoiceCardSurface,
        border = BorderStroke(2.5.dp, VoiceCardBorder),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onVoiceClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .scale(pulseScale)
                    .testTag("voice_check_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp,
                ),
            ) {
                Icon(
                    imageVector = if (voiceState.isListening) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                    contentDescription = stringResource(R.string.cd_voice_mic_icon),
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (voiceState.isListening) {
                        stringResource(R.string.voice_button_listening)
                    } else {
                        stringResource(R.string.voice_button_idle)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }

            Text(
                text = stringResource(R.string.voice_guide_examples),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkSecondary,
                ),
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            AnimatedVisibility(visible = voiceState.feedbackMessage != null || voiceState.lastSpokenText != null) {
                val isError = voiceState.isFeedbackError
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_feedback_banner"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isError) VoiceFeedbackErrorBg else MedicineGreenSoft,
                    border = BorderStroke(
                        1.5.dp,
                        if (isError) VoiceListeningRed.copy(alpha = 0.45f) else MedicineGreen.copy(alpha = 0.45f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (!voiceState.lastSpokenText.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.voice_heard_format, voiceState.lastSpokenText),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InkSecondary,
                                ),
                            )
                        }
                        if (!voiceState.feedbackMessage.isNullOrBlank()) {
                            Text(
                                text = voiceState.feedbackMessage,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isError) VoiceFeedbackErrorText else MedicineGreenDarkText,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeaderCard(
    dateTitle: String,
    dayOfWeek: String,
    takenCount: Int,
) {
    val allDone = takenCount == 3
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("date_header_card"),
        shape = RoundedCornerShape(24.dp),
        color = IvorySurface,
        border = BorderStroke(2.dp, IvoryBorder),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MedicineGreenSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = MedicineGreen,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column {
                    Text(
                        text = dateTitle,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                        ),
                        modifier = Modifier.testTag("today_date_text"),
                    )
                    Text(
                        text = dayOfWeek,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary,
                        ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (allDone) {
                        stringResource(R.string.medicine_all_done)
                    } else {
                        stringResource(R.string.header_greeting)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (allDone) MedicineGreenDarkText else InkSecondary,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (allDone) MedicineGreen else MedicineGreenSoft,
                ) {
                    Text(
                        text = stringResource(R.string.medicine_progress_format, takenCount),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allDone) Color.White else MedicineGreenDarkText,
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicineChecklistSection(
    morningTaken: Boolean,
    lunchTaken: Boolean,
    dinnerTaken: Boolean,
    onToggleMedicine: (MedicineSlot) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Medication,
                contentDescription = null,
                tint = MedicineGreen,
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = stringResource(R.string.medicine_section_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                ),
            )
        }

        MedicineActionButton(
            label = stringResource(R.string.medicine_morning),
            icon = Icons.Rounded.WbTwilight,
            iconTint = MorningAmber,
            iconBg = MorningAmberBg,
            iconDescription = stringResource(R.string.cd_morning_icon),
            isTaken = morningTaken,
            onClick = { onToggleMedicine(MedicineSlot.MORNING) },
            testTag = "medicine_button_morning",
        )

        MedicineActionButton(
            label = stringResource(R.string.medicine_lunch),
            icon = Icons.Rounded.WbSunny,
            iconTint = LunchOrange,
            iconBg = LunchOrangeBg,
            iconDescription = stringResource(R.string.cd_lunch_icon),
            isTaken = lunchTaken,
            onClick = { onToggleMedicine(MedicineSlot.LUNCH) },
            testTag = "medicine_button_lunch",
        )

        MedicineActionButton(
            label = stringResource(R.string.medicine_dinner),
            icon = Icons.Rounded.NightsStay,
            iconTint = DinnerIndigo,
            iconBg = DinnerIndigoBg,
            iconDescription = stringResource(R.string.cd_dinner_icon),
            isTaken = dinnerTaken,
            onClick = { onToggleMedicine(MedicineSlot.DINNER) },
            testTag = "medicine_button_dinner",
        )
    }
}

@Composable
private fun MedicineActionButton(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    iconDescription: String,
    isTaken: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val haptic = LocalHapticFeedback.current
    val containerColor by animateColorAsState(
        targetValue = if (isTaken) MedicineGreen else IvorySurface,
        animationSpec = tween(durationMillis = 220),
        label = "medicine_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isTaken) MedicineGreenBorder else IvoryBorder,
        animationSpec = tween(durationMillis = 220),
        label = "medicine_border",
    )
    val scale by animateFloatAsState(
        targetValue = if (isTaken) 1.0f else 0.995f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "medicine_scale",
    )
    val statusText = if (isTaken) {
        stringResource(R.string.medicine_taken_label)
    } else {
        stringResource(R.string.medicine_pending_hint)
    }
    val accessibilityStatus = if (isTaken) {
        stringResource(R.string.cd_check_done)
    } else {
        stringResource(R.string.cd_check_pending)
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .scale(scale)
            .testTag(testTag)
            .semantics {
                role = Role.Button
                contentDescription = "$label, $accessibilityStatus"
            },
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(2.5.dp, borderColor),
        shadowElevation = if (isTaken) 4.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTaken) Color.White.copy(alpha = 0.22f) else iconBg
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = if (isTaken) Color.White else iconTint,
                        modifier = Modifier.size(34.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTaken) Color.White else InkPrimary,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = if (isTaken) FontWeight.Bold else FontWeight.Medium,
                            color = if (isTaken) Color.White.copy(alpha = 0.95f) else InkSecondary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isTaken) Color.White else Color.Transparent)
                    .border(
                        width = 3.dp,
                        color = if (isTaken) Color.White else IvoryBorder,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isTaken) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.cd_check_done),
                        tint = MedicineGreen,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterTrackerCard(
    waterCups: Int,
    goalCups: Int,
    onAddWaterCup: () -> Unit,
    onRemoveWaterCup: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val goalReached = waterCups >= goalCups
    val waterSectionDescription = stringResource(R.string.cd_water_cups_format, waterCups)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("water_tracker_card"),
        shape = RoundedCornerShape(24.dp),
        color = WaterCardSurface,
        border = BorderStroke(2.5.dp, WaterCardBorder),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.water_section_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                        ),
                    )
                    Text(
                        text = if (goalReached) {
                            stringResource(R.string.water_goal_reached)
                        } else {
                            stringResource(R.string.water_goal_hint)
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (goalReached) MedicineGreenDarkText else InkSecondary,
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .testTag("water_count_text")
                        .semantics(mergeDescendants = true) {
                            contentDescription = waterSectionDescription
                        },
                ) {
                    AnimatedContent(
                        targetState = waterCups,
                        label = "water_count_anim",
                    ) { cups ->
                        Text(
                            text = stringResource(R.string.water_count_format, cups),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WaterBlue,
                            ),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = waterSectionDescription
                    },
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (cupIndex in 0 until 4) {
                        WaterGlassIcon(
                            isFilled = cupIndex < waterCups,
                            modifier = Modifier.size(width = 50.dp, height = 58.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (cupIndex in 4 until 8) {
                        WaterGlassIcon(
                            isFilled = cupIndex < waterCups,
                            modifier = Modifier.size(width = 50.dp, height = 58.dp),
                        )
                    }
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddWaterCup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 78.dp)
                    .testTag("water_add_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WaterBlue,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalDrink,
                    contentDescription = stringResource(R.string.cd_water_button_icon),
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.water_add_button),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }

            if (waterCups > 0) {
                OutlinedButton(
                    onClick = onRemoveWaterCup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("water_undo_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, WaterCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = IvorySurface,
                        contentColor = InkSecondary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.water_undo_button),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterGlassIcon(
    isFilled: Boolean,
    modifier: Modifier = Modifier,
) {
    val fillProgress by animateFloatAsState(
        targetValue = if (isFilled) 0.78f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "water_cup_fill",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val topLeftX = w * 0.12f
        val topRightX = w * 0.88f
        val bottomLeftX = w * 0.22f
        val bottomRightX = w * 0.78f
        val topY = h * 0.08f
        val bottomY = h * 0.92f

        val glassPath = Path().apply {
            moveTo(topLeftX, topY)
            lineTo(topRightX, topY)
            lineTo(bottomRightX, bottomY)
            lineTo(bottomLeftX, bottomY)
            close()
        }

        drawPath(
            path = glassPath,
            color = WaterCupEmpty.copy(alpha = 0.55f),
        )

        if (fillProgress > 0.01f) {
            clipPath(glassPath) {
                val waterHeight = (bottomY - topY) * fillProgress
                val waterTopY = bottomY - waterHeight

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(WaterBlueLight, WaterBlueBright, WaterBlue),
                        startY = waterTopY,
                        endY = bottomY,
                    ),
                    topLeft = Offset(0f, waterTopY),
                    size = Size(w, waterHeight + 4f),
                )

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(w * 0.16f, waterTopY),
                    size = Size(w * 0.68f, h * 0.06f),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }
        }

        drawPath(
            path = glassPath,
            color = if (isFilled) WaterBlue else WaterCupOutline,
            style = Stroke(
                width = w * 0.075f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
