package io.github.nanima1.twilight

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import io.github.nanima1.twilight.presentation.TimerScreen
import io.github.nanima1.twilight.presentation.TimerViewModel
import io.github.nanima1.twilight.presentation.appearance.AppearanceViewModel
import io.github.nanima1.twilight.presentation.solution.SolutionViewModel
import io.github.nanima1.twilight.presentation.theme.TwilightTimerTheme
import io.github.nanima1.twilight.domain.timer.InspectionCue
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            val timerFactory = remember { TimerViewModel.factory(applicationContext) }
            val timerViewModel: TimerViewModel = viewModel(factory = timerFactory)
            val timerState by timerViewModel.state.collectAsStateWithLifecycle()
            val hapticFeedback = LocalHapticFeedback.current
            val solutionFactory = remember {
                SolutionViewModel.factory(initialScramble = timerState.scramble)
            }
            val solutionViewModel: SolutionViewModel = viewModel(factory = solutionFactory)
            val solutionState by solutionViewModel.state.collectAsStateWithLifecycle()
            val appearanceFactory = remember { AppearanceViewModel.factory(applicationContext) }
            val appearanceViewModel: AppearanceViewModel = viewModel(factory = appearanceFactory)
            val appearanceState by appearanceViewModel.state.collectAsStateWithLifecycle()
            val wallpaperPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.toString()?.let(appearanceViewModel::importWallpaper)
            }
            LaunchedEffect(timerState.scramble) {
                solutionViewModel.solve(timerState.scramble)
            }
            LaunchedEffect(timerViewModel, hapticFeedback) {
                timerViewModel.inspectionCues.collect { cue ->
                    val pulseCount = if (cue == InspectionCue.TWELVE_SECONDS) 2 else 1
                    repeat(pulseCount) { index ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (index < pulseCount - 1) delay(120L)
                    }
                }
            }

            TwilightTimerTheme(themePreset = appearanceState.settings.themePreset) {
                TimerScreen(
                    state = timerState,
                    solution = solutionState,
                    appearance = appearanceState,
                    onTimerPressed = timerViewModel::onTimerPressed,
                    onInspectionEnabledChanged = timerViewModel::setInspectionEnabled,
                    onInspectionHapticsEnabledChanged =
                        timerViewModel::setInspectionHapticsEnabled,
                    onSolveDeleted = timerViewModel::deleteSolve,
                    onSolvePenaltyChanged = timerViewModel::setSolvePenalty,
                    onThemeSelected = appearanceViewModel::setThemePreset,
                    onWallpaperRequested = { wallpaperPicker.launch(arrayOf("image/*")) },
                    onWallpaperRemoved = appearanceViewModel::removeWallpaper,
                    onWallpaperScrimChanged = appearanceViewModel::setWallpaperScrim,
                    onWallpaperPanelOpacityChanged = appearanceViewModel::setWallpaperPanelOpacity,
                    onWallpaperPositionChanged = appearanceViewModel::setWallpaperPosition,
                    onWallpaperImportErrorShown = appearanceViewModel::clearWallpaperImportError
                )
            }
        }
    }
}
