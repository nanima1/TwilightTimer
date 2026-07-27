package io.github.nanima1.twilight.data.solve

import io.github.nanima1.twilight.domain.solve.SolveRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomSolveRepositoryTest {
    @Test
    fun `persisted notes are normalized when mapped to history`() = runTest {
        val dao = FakeSolveDao(
            solves = listOf(
                SolveEntity(
                    id = 7L,
                    durationMillis = 5_000L,
                    scramble = "R U",
                    completedAtEpochMillis = 1_000L,
                    note = "  Smooth execution  "
                )
            )
        )

        val history = RoomSolveRepository(dao).history.first()

        assertEquals("Smooth execution", history.recentSolves.single().note)
    }

    @Test
    fun `setting a note normalizes content before updating room`() = runTest {
        val dao = FakeSolveDao()
        val repository = RoomSolveRepository(dao)

        repository.setNote(7L, "  Smooth execution  ")
        assertEquals(7L to "Smooth execution", dao.updatedNote)

        repository.setNote(7L, "  ")
        assertEquals(7L, dao.updatedNote?.first)
        assertNull(dao.updatedNote?.second)
    }

    @Test
    fun `setting a note clamps content to the domain maximum`() = runTest {
        val dao = FakeSolveDao()
        val repository = RoomSolveRepository(dao)

        repository.setNote(7L, "a".repeat(SolveRecord.MAX_NOTE_LENGTH + 20))

        assertEquals(
            "a".repeat(SolveRecord.MAX_NOTE_LENGTH),
            dao.updatedNote?.second
        )
    }

    private class FakeSolveDao(
        solves: List<SolveEntity> = emptyList()
    ) : SolveDao {
        private val recent = MutableStateFlow(solves)
        private val stats = MutableStateFlow(
            SolveStatsEntity(
                solveCount = solves.size.toLong(),
                bestSolveMillis = solves.minOfOrNull(SolveEntity::durationMillis)
            )
        )
        var updatedNote: Pair<Long, String?>? = null

        override fun observeRecent(limit: Int): Flow<List<SolveEntity>> = recent

        override fun observeStats(): Flow<SolveStatsEntity> = stats

        override suspend fun insert(solve: SolveEntity) {
            recent.value = listOf(solve) + recent.value
        }

        override suspend fun setPenalty(id: Long, penaltyId: String) = Unit

        override suspend fun setNote(id: Long, note: String?) {
            updatedNote = id to note
        }

        override suspend fun deleteById(id: Long) {
            recent.value = recent.value.filterNot { it.id == id }
        }
    }
}
