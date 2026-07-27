package io.github.nanima1.twilight.presentation

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.solve.SolveRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: SolveHistory,
    onSolveDeleted: (Long) -> Unit,
    onSolvePenaltyChanged: (Long, SolvePenalty) -> Unit,
    onSolveNoteChanged: (Long, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var noteEditorSolve by remember { mutableStateOf<SolveRecord?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoryStat("SOLVES", history.stats.solveCount.toString())
                HistoryStat(
                    "LAST",
                    history.stats.lastSolveMillis?.let { durationMillis ->
                        formatSolveResult(
                            durationMillis = durationMillis,
                            penalty = history.stats.lastSolvePenalty ?: SolvePenalty.NONE
                        )
                    } ?: "--"
                )
                HistoryStat("BEST", history.stats.bestSolveMillis?.let(::formatDuration) ?: "--")
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            if (history.recentSolves.isEmpty()) {
                Text(
                    text = "No solves yet",
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
private fun HistoryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            color = MaterialTheme.colorScheme.primary
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
