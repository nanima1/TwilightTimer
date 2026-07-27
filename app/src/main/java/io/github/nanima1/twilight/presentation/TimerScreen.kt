package io.github.nanima1.twilight.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.timer.InspectionRules
import io.github.nanima1.twilight.domain.timer.TimerInputState
import io.github.nanima1.twilight.domain.timer.TimerPhase
import io.github.nanima1.twilight.domain.timer.TimerSession
import io.github.nanima1.twilight.presentation.appearance.AppearanceSheet
import io.github.nanima1.twilight.presentation.appearance.AppearanceUiState
import io.github.nanima1.twilight.presentation.settings.TimerSettingsSheet
import io.github.nanima1.twilight.presentation.solution.SolutionUiState
import java.util.Locale

@Composable
fun TimerScreen(
    state: TimerUiState,
    solution: SolutionUiState,
    appearance: AppearanceUiState,
    onTimerPressed: () -> Unit,
    onTimerPressStarted: () -> Unit,
    onTimerReleased: () -> Unit,
    onTimerPressCancelled: () -> Unit,
    onInspectionEnabledChanged: (Boolean) -> Unit,
    onInspectionHapticsEnabledChanged: (Boolean) -> Unit,
    onSolveDeleted: (Long) -> Unit,
    onSolvePenaltyChanged: (Long, SolvePenalty) -> Unit,
    onSolveNoteChanged: (Long, String?) -> Unit,
    onThemeSelected: (ThemePreset) -> Unit,
    onWallpaperRequested: () -> Unit,
    onWallpaperRemoved: () -> Unit,
    onWallpaperScrimChanged: (Float) -> Unit,
    onWallpaperPanelOpacityChanged: (Float) -> Unit,
    onWallpaperPositionChanged: (WallpaperPosition) -> Unit,
    onWallpaperImportErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showTimerSettings by rememberSaveable { mutableStateOf(false) }
    val hasWallpaper = appearance.settings.wallpaperUri != null
    val panelOpacity = if (hasWallpaper) {
        appearance.settings.wallpaperPanelOpacity
    } else {
        DEFAULT_PANEL_OPACITY_WITHOUT_WALLPAPER
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer()
        ) {
            appearance.settings.wallpaperUri?.let { wallpaperUri ->
                AsyncImage(
                    model = wallpaperUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = appearance.settings.wallpaperPosition.toAlignment(),
                    colorFilter = ColorFilter.tint(
                        color = Color.Black.copy(alpha = appearance.settings.wallpaperScrim),
                        blendMode = BlendMode.SrcOver
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (!hasWallpaper) {
                TwilightBackdrop(Modifier.fillMaxSize())
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val compactLayout = maxHeight < 700.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (compactLayout) 8.dp else 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Header(
                    onHistoryPressed = { showHistory = true },
                    onTimerSettingsPressed = { showTimerSettings = true },
                    onAppearancePressed = { showAppearance = true }
                )
                Spacer(Modifier.height(if (compactLayout) 10.dp else 20.dp))
                ScramblePanel(
                    scramble = state.scramble,
                    panelOpacity = panelOpacity,
                    compactLayout = compactLayout
                )
                Spacer(Modifier.height(if (compactLayout) 8.dp else 10.dp))
                SolutionPanel(
                    state = solution,
                    panelOpacity = panelOpacity,
                    compactLayout = compactLayout
                )
                Spacer(Modifier.weight(1f))
                TimerReadout(
                    session = state.session,
                    inputState = state.inputState,
                    inspectionEnabled = state.timerSettings.inspectionEnabled,
                    onTimerPressed = onTimerPressed,
                    onTimerPressStarted = onTimerPressStarted,
                    onTimerReleased = onTimerReleased,
                    onTimerPressCancelled = onTimerPressCancelled,
                    compactLayout = compactLayout
                )
                if (compactLayout) {
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(20.dp))
                    SessionStats(state, compactLayout = false)
                    Spacer(Modifier.height(18.dp))
                }
                val timerPhase = state.session.phase
                val controlContainerColor = when (state.inputState) {
                    TimerInputState.HOLDING -> MaterialTheme.colorScheme.tertiary
                    TimerInputState.ARMED -> MaterialTheme.colorScheme.primary
                    TimerInputState.IDLE -> when (timerPhase) {
                        TimerPhase.READY -> MaterialTheme.colorScheme.primary
                        TimerPhase.INSPECTING -> MaterialTheme.colorScheme.secondary
                        TimerPhase.RUNNING -> MaterialTheme.colorScheme.tertiary
                    }
                }
                val controlContentColor = when (state.inputState) {
                    TimerInputState.HOLDING -> MaterialTheme.colorScheme.onTertiary
                    TimerInputState.ARMED -> MaterialTheme.colorScheme.onPrimary
                    TimerInputState.IDLE -> when (timerPhase) {
                        TimerPhase.READY -> MaterialTheme.colorScheme.onPrimary
                        TimerPhase.INSPECTING -> MaterialTheme.colorScheme.onSecondary
                        TimerPhase.RUNNING -> MaterialTheme.colorScheme.onTertiary
                    }
                }
                val actionDescription = timerActionDescription(
                    phase = timerPhase,
                    inspectionEnabled = state.timerSettings.inspectionEnabled
                )
                val pressInput = Modifier.timerPressInput(
                    contentDescription = actionDescription,
                    onTimerPressed = onTimerPressed,
                    onTimerPressStarted = onTimerPressStarted,
                    onTimerReleased = onTimerReleased,
                    onTimerPressCancelled = onTimerPressCancelled
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compactLayout) 48.dp else 56.dp)
                        .then(pressInput),
                    color = controlContainerColor,
                    contentColor = controlContentColor,
                    shape = RoundedCornerShape(6.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = timerControlLabel(
                                phase = timerPhase,
                                inputState = state.inputState,
                                inspectionEnabled = state.timerSettings.inspectionEnabled
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        if (showHistory) {
            HistorySheet(
                history = state.history,
                onSolveDeleted = onSolveDeleted,
                onSolvePenaltyChanged = onSolvePenaltyChanged,
                onSolveNoteChanged = onSolveNoteChanged,
                onDismiss = { showHistory = false }
            )
        }
        if (showAppearance) {
            AppearanceSheet(
                state = appearance,
                onDismiss = { showAppearance = false },
                onThemeSelected = onThemeSelected,
                onWallpaperRequested = onWallpaperRequested,
                onWallpaperRemoved = onWallpaperRemoved,
                onWallpaperScrimChanged = onWallpaperScrimChanged,
                onWallpaperPanelOpacityChanged = onWallpaperPanelOpacityChanged,
                onWallpaperPositionChanged = onWallpaperPositionChanged,
                onWallpaperImportErrorShown = onWallpaperImportErrorShown
            )
        }
        if (showTimerSettings) {
            TimerSettingsSheet(
                settings = state.timerSettings,
                onInspectionEnabledChanged = onInspectionEnabledChanged,
                onInspectionHapticsEnabledChanged = onInspectionHapticsEnabledChanged,
                onDismiss = { showTimerSettings = false }
            )
        }
    }
}

@Composable
private fun SolutionPanel(
    state: SolutionUiState,
    panelOpacity: Float,
    compactLayout: Boolean
) {
    val displayedSolution = when (state) {
        is SolutionUiState.Optimized -> state.optimizedSolution
        else -> state.immediateSolution
    }
    val status = when (state) {
        is SolutionUiState.Immediate -> "INSTANT"
        is SolutionUiState.Optimizing -> "OPTIMIZING"
        is SolutionUiState.Optimized -> "OPTIMIZED"
        is SolutionUiState.OptimizationFailed -> "INSTANT FALLBACK"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = panelOpacity),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            Modifier.padding(
                horizontal = 16.dp,
                vertical = if (compactLayout) 10.dp else 13.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SOLUTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (state) {
                        is SolutionUiState.Optimizing -> CircularProgressIndicator(
                            modifier = Modifier.width(13.dp).height(13.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        is SolutionUiState.Optimized -> Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.width(15.dp).height(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        is SolutionUiState.OptimizationFailed -> Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "Optimization unavailable",
                            modifier = Modifier.width(15.dp).height(15.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        is SolutionUiState.Immediate -> Unit
                    }
                    if (state !is SolutionUiState.Immediate) {
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = "$status  ${displayedSolution.moveCount} MOVES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
            Text(
                text = displayedSolution.algorithm.ifEmpty { "Solved" },
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun WallpaperPosition.toAlignment(): Alignment = when (this) {
    WallpaperPosition.TOP -> Alignment.TopCenter
    WallpaperPosition.CENTER -> Alignment.Center
    WallpaperPosition.BOTTOM -> Alignment.BottomCenter
}

@Composable
private fun Header(
    onHistoryPressed: () -> Unit,
    onTimerSettingsPressed: () -> Unit,
    onAppearancePressed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "TWILIGHT TIMER",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ALPHA",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onHistoryPressed) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = "Open solve history",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onTimerSettingsPressed) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Open timing settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onAppearancePressed) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Open appearance",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ScramblePanel(
    scramble: String,
    panelOpacity: Float,
    compactLayout: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = panelOpacity),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            Modifier.padding(
                horizontal = 16.dp,
                vertical = if (compactLayout) 12.dp else 16.dp
            )
        ) {
            Text(
                text = "3 x 3 scramble",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = scramble,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 25.sp
            )
        }
    }
}

@Composable
private fun TimerReadout(
    session: TimerSession,
    inputState: TimerInputState,
    inspectionEnabled: Boolean,
    onTimerPressed: () -> Unit,
    onTimerPressStarted: () -> Unit,
    onTimerReleased: () -> Unit,
    onTimerPressCancelled: () -> Unit,
    compactLayout: Boolean
) {
    val inspectionPenalty = InspectionRules.penaltyFor(session.inspectionElapsedMillis)
    val timerColor = when (inputState) {
        TimerInputState.HOLDING -> MaterialTheme.colorScheme.tertiary
        TimerInputState.ARMED -> MaterialTheme.colorScheme.primary
        TimerInputState.IDLE -> when (session.phase) {
            TimerPhase.READY -> when (session.penalty) {
                SolvePenalty.NONE -> MaterialTheme.colorScheme.onBackground
                SolvePenalty.PLUS_TWO -> MaterialTheme.colorScheme.tertiary
                SolvePenalty.DNF -> MaterialTheme.colorScheme.error
            }
            TimerPhase.INSPECTING -> when (inspectionPenalty) {
                SolvePenalty.NONE -> MaterialTheme.colorScheme.primary
                SolvePenalty.PLUS_TWO -> MaterialTheme.colorScheme.tertiary
                SolvePenalty.DNF -> MaterialTheme.colorScheme.error
            }
            TimerPhase.RUNNING -> when (session.penalty) {
                SolvePenalty.NONE, SolvePenalty.PLUS_TWO -> MaterialTheme.colorScheme.tertiary
                SolvePenalty.DNF -> MaterialTheme.colorScheme.error
            }
        }
    }
    val readout = when (session.phase) {
        TimerPhase.READY -> formatSolveResult(session.elapsedMillis, session.penalty)
        TimerPhase.INSPECTING -> formatInspectionReadout(session.inspectionElapsedMillis)
        TimerPhase.RUNNING -> formatDuration(session.elapsedMillis)
    }
    val status = when (inputState) {
        TimerInputState.HOLDING -> "HOLD"
        TimerInputState.ARMED -> "RELEASE"
        TimerInputState.IDLE -> when (session.phase) {
            TimerPhase.READY -> "READY"
            TimerPhase.INSPECTING -> when (inspectionPenalty) {
                SolvePenalty.NONE -> "INSPECTION"
                SolvePenalty.PLUS_TWO -> "+2 PENALTY"
                SolvePenalty.DNF -> "DNF"
            }
            TimerPhase.RUNNING -> when (session.penalty) {
                SolvePenalty.NONE -> "SOLVING"
                SolvePenalty.PLUS_TWO -> "SOLVING +2"
                SolvePenalty.DNF -> "SOLVING DNF"
            }
        }
    }
    val actionDescription = timerActionDescription(session.phase, inspectionEnabled)
    val pressInput = Modifier.timerPressInput(
        contentDescription = actionDescription,
        onTimerPressed = onTimerPressed,
        onTimerPressStarted = onTimerPressStarted,
        onTimerReleased = onTimerReleased,
        onTimerPressCancelled = onTimerPressCancelled
    )
    val readoutFontSize = when {
        readout.length >= 9 -> if (compactLayout) 44.sp else 52.sp
        readout.length >= 7 -> if (compactLayout) 52.sp else 60.sp
        else -> if (compactLayout) 60.sp else 68.sp
    }
    val readoutLineHeight = when {
        readout.length >= 9 -> if (compactLayout) 50.sp else 58.sp
        readout.length >= 7 -> if (compactLayout) 58.sp else 66.sp
        else -> if (compactLayout) 66.sp else 74.sp
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer()
            .clip(RoundedCornerShape(6.dp))
            .then(pressInput)
            .padding(
                vertical = if (compactLayout) 8.dp else 18.dp,
                horizontal = 8.dp
            )
    ) {
        Text(
            text = readout,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = readoutFontSize,
            lineHeight = readoutLineHeight,
            color = timerColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = MaterialTheme.typography.displayLarge.copy(
                shadow = Shadow(
                    color = timerColor.copy(alpha = 0.35f),
                    blurRadius = 18f
                )
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(if (compactLayout) 4.dp else 8.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            color = timerColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Modifier.timerPressInput(
    contentDescription: String,
    onTimerPressed: () -> Unit,
    onTimerPressStarted: () -> Unit,
    onTimerReleased: () -> Unit,
    onTimerPressCancelled: () -> Unit
): Modifier {
    val currentOnTimerPressed by rememberUpdatedState(onTimerPressed)
    val currentOnTimerPressStarted by rememberUpdatedState(onTimerPressStarted)
    val currentOnTimerReleased by rememberUpdatedState(onTimerReleased)
    val currentOnTimerPressCancelled by rememberUpdatedState(onTimerPressCancelled)

    return this
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consume()
                currentOnTimerPressStarted()
                val up = waitForUpOrCancellation()
                if (up == null) {
                    currentOnTimerPressCancelled()
                } else {
                    up.consume()
                    currentOnTimerReleased()
                }
            }
        }
        .semantics {
            role = Role.Button
            this.contentDescription = contentDescription
            onClick {
                currentOnTimerPressed()
                true
            }
        }
}

private fun timerActionDescription(
    phase: TimerPhase,
    inspectionEnabled: Boolean
): String = when (phase) {
    TimerPhase.READY -> if (inspectionEnabled) "Start inspection" else "Start solve"
    TimerPhase.INSPECTING -> "Start solve"
    TimerPhase.RUNNING -> "Stop timer"
}

private fun timerControlLabel(
    phase: TimerPhase,
    inputState: TimerInputState,
    inspectionEnabled: Boolean
): String = when (inputState) {
    TimerInputState.HOLDING -> "Keep holding"
    TimerInputState.ARMED -> if (phase == TimerPhase.READY && inspectionEnabled) {
        "Release for inspection"
    } else {
        "Release to start"
    }
    TimerInputState.IDLE -> when (phase) {
        TimerPhase.READY -> if (inspectionEnabled) "Hold for inspection" else "Hold to start"
        TimerPhase.INSPECTING -> "Hold to start"
        TimerPhase.RUNNING -> "Touch to stop"
    }
}

@Composable
private fun SessionStats(
    state: TimerUiState,
    compactLayout: Boolean
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Square
                )
            }
            .padding(top = if (compactLayout) 10.dp else 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Stat("SOLVES", state.history.stats.solveCount.toString())
        Stat(
            "LAST",
            state.history.stats.lastSolveMillis?.let { durationMillis ->
                formatSolveResult(
                    durationMillis = durationMillis,
                    penalty = state.history.stats.lastSolvePenalty ?: SolvePenalty.NONE
                )
            } ?: "--"
        )
        Stat("BEST", state.history.stats.bestSolveMillis?.let(::formatDuration) ?: "--")
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TwilightBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cyan = Color(0xFF27E2E8).copy(alpha = 0.15f)
        val pink = Color(0xFFF15DB3).copy(alpha = 0.13f)
        val yellow = Color(0xFFF8D06A).copy(alpha = 0.12f)
        val stroke = Stroke(width = 2.dp.toPx())
        drawLine(cyan, start = androidx.compose.ui.geometry.Offset(size.width * 0.06f, size.height * 0.14f), end = androidx.compose.ui.geometry.Offset(size.width * 0.38f, size.height * 0.03f), strokeWidth = 2.dp.toPx())
        drawLine(pink, start = androidx.compose.ui.geometry.Offset(size.width * 0.68f, size.height * 0.2f), end = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.09f), strokeWidth = 2.dp.toPx())
        drawRect(yellow, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.77f, size.height * 0.75f), size = androidx.compose.ui.geometry.Size(size.width * 0.15f, size.width * 0.15f), style = stroke)
        drawRect(cyan, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.04f, size.height * 0.82f), size = androidx.compose.ui.geometry.Size(size.width * 0.18f, size.width * 0.18f), style = stroke)
    }
}

internal fun formatDuration(durationMillis: Long): String {
    val centiseconds = (durationMillis / 10L) % 100L
    val totalSeconds = durationMillis / 1_000L
    val seconds = totalSeconds % 60L
    val minutes = totalSeconds / 60L
    return if (minutes > 0) {
        String.format(Locale.US, "%d:%02d.%02d", minutes, seconds, centiseconds)
    } else {
        String.format(Locale.US, "%d.%02d", seconds, centiseconds)
    }
}

internal fun formatSolveResult(durationMillis: Long, penalty: SolvePenalty): String = when (penalty) {
    SolvePenalty.NONE -> formatDuration(durationMillis)
    SolvePenalty.PLUS_TWO -> "${formatDuration(penalty.applyTo(durationMillis) ?: 0L)} +2"
    SolvePenalty.DNF -> "DNF"
}

internal fun formatInspectionReadout(elapsedMillis: Long): String = when (
    InspectionRules.penaltyFor(elapsedMillis)
) {
    SolvePenalty.NONE -> {
        val normalizedElapsedMillis = elapsedMillis.coerceAtLeast(0L)
        val remainingMillis = (InspectionRules.LIMIT_MILLIS - normalizedElapsedMillis)
            .coerceAtLeast(0L)
        ((remainingMillis + 999L) / 1_000L).toString()
    }
    SolvePenalty.PLUS_TWO -> "+2"
    SolvePenalty.DNF -> "DNF"
}

private const val DEFAULT_PANEL_OPACITY_WITHOUT_WALLPAPER = 0.82f
