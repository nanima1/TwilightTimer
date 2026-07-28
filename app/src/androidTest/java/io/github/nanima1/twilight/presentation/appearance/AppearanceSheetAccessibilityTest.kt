package io.github.nanima1.twilight.presentation.appearance

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.nanima1.twilight.presentation.theme.TwilightTimerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppearanceSheetAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun slidersExposeNamesAndCurrentPercentages() {
        composeRule.setContent {
            TwilightTimerTheme {
                AppearanceSheet(
                    state = AppearanceUiState(),
                    onDismiss = {},
                    onThemeSelected = {},
                    onWallpaperRequested = {},
                    onWallpaperRemoved = {},
                    onWallpaperScrimChanged = {},
                    onWallpaperPanelOpacityChanged = {},
                    onWallpaperPositionChanged = {},
                    onWallpaperImportErrorShown = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Background darkness")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "58 percent"
                )
            )
            .assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Panel opacity")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "90 percent"
                )
            )
            .assertIsNotEnabled()
    }
}
