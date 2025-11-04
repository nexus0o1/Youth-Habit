package com.youth.habittracker.data.local.dao

import androidx.room.*
import com.youth.habittracker.data.local.entity.HabitEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {
    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    fun getHabitEntries(habitId: String): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE entryId = :entryId")
    suspend fun getHabitEntryById(entryId: String): HabitEntryEntity?

    @Query("SELECT * FROM habit_entries WHERE userId = :userId AND date >= :startDate AND date < :endDate ORDER BY date DESC")
    suspend fun getEntriesInDateRange(userId: String, startDate: Long, endDate: Long): List<HabitEntryEntity>

    @Query("SELECT * FROM habit_entries WHERE userId = :userId AND completed = 1 ORDER BY date DESC")
    fun getCompletedEntries(userId: String): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE userId = :userId AND date >= :startDate AND completed = 1")
    suspend fun getCompletedEntriesSince(userId: String, startDate: Long): List<HabitEntryEntity>

    @Query("SELECT * FROM habit_entries WHERE userId = :userId AND habitId = :habitId AND date = :date")
    suspend fun getEntryForDate(userId: String, habitId: String, date: Long): HabitEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitEntry(entry: HabitEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitEntries(entries: List<HabitEntryEntity>)

    @Update
    suspend fun updateHabitEntry(entry: HabitEntryEntity)

    @Delete
    suspend fun deleteHabitEntry(entry: HabitEntryEntity)

    @Query("DELETE FROM habit_entries WHERE entryId = :entryId")
    suspend fun deleteHabitEntryById(entryId: String)

    @Query("SELECT * FROM habit_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<HabitEntryEntity>

    @Query("UPDATE habit_entries SET isSynced = 1 WHERE entryId = :entryId")
    suspend fun markAsSynced(entryId: String)

    @Query("DELETE FROM habit_entries WHERE userId = :userId")
    suspend fun deleteAllUserEntries(userId: String)

    @Query("SELECT COUNT(*) FROM habit_entries WHERE habitId = :habitId AND completed = 1")
    suspend fun getCompletedCount(habitId: String): Int

    @Query("SELECT COUNT(*) FROM habit_entries WHERE habitId = :habitId AND completed = 1 AND date >= :startDate")
    suspend fun getCompletedCountSince(habitId: String, startDate: Long): Int
}