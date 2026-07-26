package io.github.nanima1.twilight

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import io.github.nanima1.twilight.presentation.TimerScreen
import io.github.nanima1.twilight.presentation.TimerViewModel
import io.github.nanima1.twilight.presentation.appearance.AppearanceViewModel
import io.github.nanima1.twilight.presentation.theme.TwilightTimerTheme

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
            val appearanceFactory = remember { AppearanceViewModel.factory(applicationContext) }
            val appearanceViewModel: AppearanceViewModel = viewModel(factory = appearanceFactory)
            val appearanceState by appearanceViewModel.state.collectAsStateWithLifecycle()
            val wallpaperPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.toString()?.let(appearanceViewModel::importWallpaper)
            }

            TwilightTimerTheme(themePreset = appearanceState.settings.themePreset) {
                TimerScreen(
                    state = timerState,
                    appearance = appearanceState,
                    onTimerPressed = timerViewModel::onTimerPressed,
                    onSolveDeleted = timerViewModel::deleteSolve,
                    onThemeSelected = appearanceViewModel::setThemePreset,
                    onWallpaperRequested = { wallpaperPicker.launch(arrayOf("image/*")) },
                    onWallpaperRemoved = appearanceViewModel::removeWallpaper,
                    onWallpaperScrimChanged = appearanceViewModel::setWallpaperScrim,
                    onWallpaperPositionChanged = appearanceViewModel::setWallpaperPosition,
                    onWallpaperImportErrorShown = appearanceViewModel::clearWallpaperImportError
                )
            }
        }
    }
}
