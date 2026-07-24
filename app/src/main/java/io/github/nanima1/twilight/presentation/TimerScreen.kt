package io.github.nanima1.twilight.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nanima1.twilight.domain.timer.TimerPhase
import java.util.Locale

@Composable
fun TimerScreen(
    state: TimerUiState,
    onTimerPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TwilightBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header()
            Spacer(Modifier.height(20.dp))
            ScramblePanel(scramble = state.scramble)
            Spacer(Modifier.weight(1f))
            TimerReadout(
                elapsedMillis = state.session.elapsedMillis,
                isRunning = state.session.phase == TimerPhase.RUNNING,
                onTimerPressed = onTimerPressed
            )
            Spacer(Modifier.height(20.dp))
            SessionStats(state)
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onTimerPressed,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.session.phase == TimerPhase.RUNNING) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (state.session.phase == TimerPhase.RUNNING) "Stop solve" else "Start timer",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun Header() {
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
        Text(
            text = "ALPHA",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScramblePanel(scramble: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
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
    elapsedMillis: Long,
    isRunning: Boolean,
    onTimerPressed: () -> Unit
) {
    val timerColor = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onTimerPressed)
            .semantics {
                role = Role.Button
                contentDescription = if (isRunning) "Stop timer" else "Start timer"
            }
            .padding(vertical = 18.dp, horizontal = 8.dp)
    ) {
        Text(
            text = formatDuration(elapsedMillis),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 68.sp,
            lineHeight = 74.sp,
            color = timerColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isRunning) "SOLVING" else "READY",
            style = MaterialTheme.typography.labelMedium,
            color = timerColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SessionStats(state: TimerUiState) {
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
            .padding(top = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Stat("SOLVES", state.session.solveCount.toString())
        Stat("LAST", state.session.lastSolveMillis?.let(::formatDuration) ?: "--")
        Stat("BEST", state.session.bestSolveMillis?.let(::formatDuration) ?: "--")
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

private fun formatDuration(durationMillis: Long): String {
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
