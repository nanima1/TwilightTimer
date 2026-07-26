package io.github.nanima1.twilight.data.solve

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SolveEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TwilightDatabase : RoomDatabase() {
    abstract fun solveDao(): SolveDao

    companion object {
        @Volatile
        private var instance: TwilightDatabase? = null

        fun getInstance(context: Context): TwilightDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                TwilightDatabase::class.java,
                "twilight_timer.db"
            ).build().also { instance = it }
        }
    }
}
