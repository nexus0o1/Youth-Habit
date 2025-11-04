package com.youth.habittracker.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.youth.habittracker.data.local.dao.DiaryEntryDao
import com.youth.habittracker.data.local.dao.HabitDao
import com.youth.habittracker.data.local.dao.HabitEntryDao
import com.youth.habittracker.data.local.dao.UserDao
import com.youth.habittracker.data.local.entity.DiaryEntryEntity
import com.youth.habittracker.data.local.entity.HabitEntity
import com.youth.habittracker.data.local.entity.HabitEntryEntity
import com.youth.habittracker.data.local.entity.UserEntity
import com.youth.habittracker.data.local.database.converters.Converters

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        HabitEntryEntity::class,
        DiaryEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun diaryEntryDao(): DiaryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Example migration for future use
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns or tables here when needed
                // database.execSQL("ALTER TABLE habits ADD COLUMN new_column TEXT")
            }
        }
    }
}