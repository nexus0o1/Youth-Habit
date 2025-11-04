package com.youth.habittracker.data.local.dao

import androidx.room.*
import com.youth.habittracker.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveHabits(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isActive = 0 ORDER BY archivedAt DESC")
    fun getArchivedHabits(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getHabitsByCategory(userId: String, category: String): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE habitId = :habitId")
    suspend fun deleteHabitById(habitId: String)

    @Query("UPDATE habits SET isActive = 0, archivedAt = :timestamp WHERE habitId = :habitId")
    suspend fun archiveHabit(habitId: String, timestamp: Long)

    @Query("UPDATE habits SET isActive = 1, archivedAt = NULL WHERE habitId = :habitId")
    suspend fun unarchiveHabit(habitId: String)

    @Query("SELECT * FROM habits WHERE userId = :userId AND isActive = 1 ORDER BY lastModified DESC LIMIT :limit")
    suspend fun getRecentlyModifiedHabits(userId: String, limit: Int): List<HabitEntity>

    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteAllUserHabits(userId: String)
}