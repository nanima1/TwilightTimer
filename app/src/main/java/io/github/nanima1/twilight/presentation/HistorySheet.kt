package io.github.nanima1.twilight.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nanima1.twilight.domain.solve.SolveAverage
import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolveHistoryFilter
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.solve.SolveRecord
import io.github.nanima1.twilight.domain.solve.SolveTrendPoint
import io.github.nanima1.twilight.domain.solve.buildSolveTrend
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: SolveHistory,
    selectedFilter: SolveHistoryFilter,
    onSolveDeleted: (Long) -> Unit,
    onSolvePenaltyChanged: (Long, SolvePenalty) -> Unit,
    onSolveNoteChanged: (Long, String?) -> Unit,
    onFilterSelected: (SolveHistoryFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var noteEditorSolve by remember { mutableStateOf<SolveRecord?>(null) }
    val trendPoints = remember(history.recentSolves) {
        buildSolveTrend(history.recentSolves)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Solve history",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close solve history")
                }
            }

            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SolveHistoryFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = filter == selectedFilter,
                        onClick = { onFilterSelected(filter) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = SolveHistoryFilter.entries.size
                        )
                    ) {
                        Text(filter.shortLabel())
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryStat(
                    label = "SOLVES",
                    value = history.stats.solveCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                HistoryStat(
                    label = "LAST",
                    value = history.stats.lastSolveMillis?.let { durationMillis ->
                        formatSolveResult(
                            durationMillis = durationMillis,
                            penalty = history.stats.lastSolvePenalty ?: SolvePenalty.NONE
                        )
                    } ?: "--",
                    modifier = Modifier.weight(1f),
                    isDnf = history.stats.lastSolvePenalty == SolvePenalty.DNF
                )
                HistoryStat(
                    label = "BEST",
                    value = history.stats.bestSolveMillis?.let(::formatDuration) ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                HistoryStat(
                    label = "AO5",
                    value = history.stats.averageOf5.displayValue(),
                    modifier = Modifier.weight(1f),
                    isDnf = history.stats.averageOf5 == SolveAverage.Dnf
                )
                HistoryStat(
                    label = "AO12",
                    value = history.stats.averageOf12.displayValue(),
                    modifier = Modifier.weight(1f),
                    isDnf = history.stats.averageOf12 == SolveAverage.Dnf
                )
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            if (history.recentSolves.isEmpty()) {
                Text(
                    text = "No solves in this range",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "solve-trend") {
                        SolveTrendChart(points = trendPoints)
                    }
                    items(
                        items = history.recentSolves,
                        key = SolveRecord::id
                    ) { solve ->
                        SolveHistoryItem(
                            solve = solve,
                            onPenaltyChanged = { penalty ->
                                onSolvePenaltyChanged(solve.id, penalty)
                            },
                            onEditNote = { noteEditorSolve = solve },
                            onDelete = { onSolveDeleted(solve.id) }
                        )
                    }
                }
            }
        }
    }
    noteEditorSolve?.let { solve ->
        SolveNoteDialog(
            solve = solve,
            onSave = { note -> onSolveNoteChanged(solve.id, note) },
            onDismiss = { noteEditorSolve = null }
        )
    }
}

@Composable
private fun SolveTrendChart(
    points: List<SolveTrendPoint>,
    modifier: Modifier = Modifier
) {
    val validDurations = remember(points) {
        points.mapNotNull(SolveTrendPoint::adjustedDurationMillis)
    }
    val chartDescription = remember(points) { trendDescription(points) }
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val dnfColor = MaterialTheme.colorScheme.error

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TREND",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (points.size) {
                    1 -> "1 RESULT"
                    20 -> "LAST 20"
                    else -> "${points.size} RESULTS"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .semantics { contentDescription = chartDescription }
        ) {
            val horizontalInset = 8.dp.toPx()
            val verticalInset = 10.dp.toPx()
            val chartLeft = horizontalInset
            val chartRight = size.width - horizontalInset
            val chartTop = verticalInset
            val chartBottom = size.height - verticalInset
            val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
            val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

            repeat(3) { index ->
                val y = chartTop + chartHeight * index / 2f
                drawLine(
                    color = gridColor,
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 0.75.dp.toPx()
                )
            }

            val minimumDuration = validDurations.minOrNull()?.toFloat() ?: 0f
            val maximumDuration = validDurations.maxOrNull()?.toFloat() ?: minimumDuration
            val rawRange = maximumDuration - minimumDuration
            val durationRange = rawRange.coerceAtLeast(100f)
            val lowerDuration = minimumDuration - (durationRange - rawRange) / 2f

            fun xFor(index: Int): Float = if (points.size == 1) {
                chartLeft + chartWidth / 2f
            } else {
                chartLeft + chartWidth * index / (points.size - 1).coerceAtLeast(1)
            }

            fun yFor(durationMillis: Long): Float {
                val ratio = ((durationMillis - lowerDuration) / durationRange).coerceIn(0f, 1f)
                return chartBottom - ratio * chartHeight
            }

            val offsets = points.mapIndexed { index, point ->
                point.adjustedDurationMillis?.let { duration ->
                    Offset(xFor(index), yFor(duration))
                }
            }
            var previousOffset: Offset? = null
            offsets.forEach { offset ->
                if (offset == null) {
                    previousOffset = null
                } else {
                    previousOffset?.let { previous ->
                        drawLine(
                            color = lineColor,
                            start = previous,
                            end = offset,
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    previousOffset = offset
                }
            }
            offsets.forEachIndexed { index, offset ->
                if (offset != null) {
                    drawCircle(
                        color = lineColor,
                        radius = 3.5.dp.toPx(),
                        center = offset
                    )
                } else {
                    val center = Offset(xFor(index), chartTop + 5.dp.toPx())
                    val radius = 4.dp.toPx()
                    drawLine(
                        color = dnfColor,
                        start = Offset(center.x - radius, center.y - radius),
                        end = Offset(center.x + radius, center.y + radius),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = dnfColor,
                        start = Offset(center.x - radius, center.y + radius),
                        end = Offset(center.x + radius, center.y - radius),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun HistoryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isDnf: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isDnf) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun SolveHistoryItem(
    solve: SolveRecord,
    onPenaltyChanged: (SolvePenalty) -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit
) {
    var showPenaltyMenu by remember(solve.id) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSolveResult(solve.durationMillis, solve.penalty),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when (solve.penalty) {
                        SolvePenalty.NONE -> MaterialTheme.colorScheme.onSurface
                        SolvePenalty.PLUS_TWO -> MaterialTheme.colorScheme.tertiary
                        SolvePenalty.DNF -> MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = formatCompletedAt(solve.completedAtEpochMillis),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box {
                    IconButton(onClick = { showPenaltyMenu = true }) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Open solve actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showPenaltyMenu,
                        onDismissRequest = { showPenaltyMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (solve.note == null) "Add note" else "Edit note")
                            },
                            onClick = {
                                showPenaltyMenu = false
                                onEditNote()
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.EditNote, contentDescription = null)
                            }
                        )
                        HorizontalDivider()
                        SolvePenalty.entries.forEach { penalty ->
                            DropdownMenuItem(
                                text = { Text(penalty.menuLabel()) },
                                onClick = {
                                    showPenaltyMenu = false
                                    onPenaltyChanged(penalty)
                                },
                                trailingIcon = if (penalty == solve.penalty) {
                                    {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete solve",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = solve.scramble,
                modifier = Modifier.padding(end = 14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            solve.note?.let { note ->
                Text(
                    text = note,
                    modifier = Modifier.padding(top = 6.dp, end = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SolveNoteDialog(
    solve: SolveRecord,
    onSave: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember(solve.id, solve.note) { mutableStateOf(solve.note.orEmpty()) }
    val normalizedNote = SolveRecord.normalizeNote(note)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.EditNote, contentDescription = null) },
        title = { Text("Solve note") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(SolveRecord.MAX_NOTE_LENGTH) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note") },
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "${note.length}/${SolveRecord.MAX_NOTE_LENGTH}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(note)
                    onDismiss()
                },
                enabled = normalizedNote != solve.note
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private val historyDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

private fun formatCompletedAt(epochMillis: Long): String = Instant
    .ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault())
    .format(historyDateFormatter)

private fun SolvePenalty.menuLabel(): String = when (this) {
    SolvePenalty.NONE -> "No penalty"
    SolvePenalty.PLUS_TWO -> "+2 seconds"
    SolvePenalty.DNF -> "DNF"
}

private fun SolveHistoryFilter.shortLabel(): String = when (this) {
    SolveHistoryFilter.ALL -> "All"
    SolveHistoryFilter.TODAY -> "Today"
    SolveHistoryFilter.LAST_7_DAYS -> "7D"
    SolveHistoryFilter.LAST_30_DAYS -> "30D"
}

private fun SolveAverage.displayValue(): String = when (this) {
    is SolveAverage.Time -> formatDuration(durationMillis)
    SolveAverage.Dnf -> "DNF"
    SolveAverage.Unavailable -> "--"
}

private fun trendDescription(points: List<SolveTrendPoint>): String {
    val validDurations = points.mapNotNull(SolveTrendPoint::adjustedDurationMillis)
    val dnfCount = points.size - validDurations.size
    return buildString {
        val resultLabel = if (points.size == 1) "result" else "results"
        append("Solve trend for ${points.size} $resultLabel.")
        if (validDurations.isNotEmpty()) {
            append(" Fastest ${formatDuration(validDurations.min())}.")
            append(" Slowest ${formatDuration(validDurations.max())}.")
        }
        if (dnfCount > 0) {
            val dnfLabel = if (dnfCount == 1) "result" else "results"
            append(" $dnfCount DNF $dnfLabel.")
        }
    }
}
