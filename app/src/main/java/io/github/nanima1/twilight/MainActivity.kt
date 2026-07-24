package io.github.nanima1.twilight

import android.os.Bundle
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import io.github.nanima1.twilight.presentation.TimerScreen
import io.github.nanima1.twilight.presentation.TimerViewModel
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
            val viewModel: TimerViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            TwilightTimerTheme {
                TimerScreen(
                    state = state,
                    onTimerPressed = viewModel::onTimerPressed
                )
            }
        }
    }
}
