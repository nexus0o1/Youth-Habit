package com.youth.habittracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.youth.habittracker.data.model.Habit
import com.youth.habittracker.data.model.HabitEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun getActiveHabits(): Flow<List<Habit>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = firestore.collection("habits")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Habit::class.java)?.copy(habitId = document.id)
                } ?: emptyList()

                trySend(habits)
            }

        awaitClose { subscription.remove() }
    }

    fun getArchivedHabits(): Flow<List<Habit>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = firestore.collection("habits")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isActive", false)
            .orderBy("archivedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Habit::class.java)?.copy(habitId = document.id)
                } ?: emptyList()

                trySend(habits)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createHabit(habit: Habit): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val habitWithUserId = habit.copy(userId = currentUserId)

            val documentReference = firestore.collection("habits")
                .add(habitWithUserId)
                .await()

            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHabit(habit: Habit): Result<Unit> {
        return try {
            firestore.collection("habits")
                .document(habit.habitId)
                .set(habit)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            firestore.collection("habits")
                .document(habitId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveHabit(habitId: String): Result<Unit> {
        return try {
            firestore.collection("habits")
                .document(habitId)
                .update("isActive", false, "archivedAt", com.google.firebase.Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unarchiveHabit(habitId: String): Result<Unit> {
        return try {
            firestore.collection("habits")
                .document(habitId)
                .update("isActive", true, "archivedAt", null)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getHabitEntries(habitId: String): Flow<List<HabitEntry>> = callbackFlow {
        val subscription = firestore.collection("habitEntries")
            .whereEqualTo("habitId", habitId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(HabitEntry::class.java)?.copy(entryId = document.id)
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createHabitEntry(entry: HabitEntry): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val entryWithUserId = entry.copy(userId = currentUserId)

            val documentReference = firestore.collection("habitEntries")
                .add(entryWithUserId)
                .await()

            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHabitEntry(entry: HabitEntry): Result<Unit> {
        return try {
            firestore.collection("habitEntries")
                .document(entry.entryId)
                .set(entry)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHabitEntry(entryId: String): Result<Unit> {
        return try {
            firestore.collection("habitEntries")
                .document(entryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabitById(habitId: String): Result<Habit?> {
        return try {
            val document = firestore.collection("habits")
                .document(habitId)
                .get()
                .await()

            val habit = document.toObject(Habit::class.java)?.copy(habitId = document.id)
            Result.success(habit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayEntries(): Result<List<HabitEntry>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val today = com.google.firebase.Timestamp.now()
            val startOfDay = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time

            val startTimestamp = com.google.firebase.Timestamp(startOfDay)

            val documents = firestore.collection("habitEntries")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThan("date", today)
                .get()
                .await()

            val entries = documents.documents.mapNotNull { document ->
                document.toObject(HabitEntry::class.java)?.copy(entryId = document.id)
            }

            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}