package com.youth.habittracker.data.local.dao

import androidx.room.*
import com.youth.habittracker.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllDiaryEntries(userId: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE entryId = :entryId")
    suspend fun getDiaryEntryById(entryId: String): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries WHERE userId = :userId AND createdAt >= :startDate AND createdAt < :endDate ORDER BY createdAt DESC")
    suspend fun getEntriesInDateRange(userId: String, startDate: Long, endDate: Long): List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE userId = :userId AND tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getEntriesByTag(userId: String, tag: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE userId = :userId AND isPrivate = 0 ORDER BY createdAt DESC")
    fun getPublicEntries(userId: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE userId = :userId AND primaryMood = :mood ORDER BY createdAt DESC")
    fun getEntriesByMood(userId: String, mood: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentEntries(userId: String, limit: Int): List<DiaryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntries(entries: List<DiaryEntryEntity>)

    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntryEntity)

    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE entryId = :entryId")
    suspend fun deleteDiaryEntryById(entryId: String)

    @Query("SELECT * FROM diary_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<DiaryEntryEntity>

    @Query("UPDATE diary_entries SET isSynced = 1 WHERE entryId = :entryId")
    suspend fun markAsSynced(entryId: String)

    @Query("DELETE FROM diary_entries WHERE userId = :userId")
    suspend fun deleteAllUserEntries(userId: String)

    @Query("SELECT COUNT(*) FROM diary_entries WHERE userId = :userId")
    suspend fun getEntryCount(userId: String): Int

    @Query("SELECT DISTINCT tags FROM diary_entries WHERE userId = :userId")
    suspend fun getAllTags(userId: String): List<String>

    @Query("SELECT DISTINCT primaryMood FROM diary_entries WHERE userId = :userId")
    suspend fun getAllMoods(userId: String): List<String>
}