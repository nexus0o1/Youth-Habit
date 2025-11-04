package com.youth.habittracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.youth.habittracker.data.model.DiaryEntry
import com.youth.habittracker.data.model.Template
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    fun getAllDiaryEntries(): Flow<List<DiaryEntry>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = firestore.collection("diaryEntries")
            .whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(DiaryEntry::class.java)?.copy(entryId = document.id)
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { subscription.remove() }
    }

    fun getPublicDiaryEntries(): Flow<List<DiaryEntry>> = callbackFlow {
        val subscription = firestore.collection("diaryEntries")
            .whereEqualTo("isPrivate", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(DiaryEntry::class.java)?.copy(entryId = document.id)
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { subscription.remove() }
    }

    fun getEntriesByTag(tag: String): Flow<List<DiaryEntry>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = firestore.collection("diaryEntries")
            .whereEqualTo("userId", currentUserId)
            .whereArrayContains("tags", tag)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(DiaryEntry::class.java)?.copy(entryId = document.id)
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { subscription.remove() }
    }

    fun getEntriesByMood(mood: String): Flow<List<DiaryEntry>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = firestore.collection("diaryEntries")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("mood.primary", mood)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(DiaryEntry::class.java)?.copy(entryId = document.id)
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createDiaryEntry(entry: DiaryEntry): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val entryWithUserId = entry.copy(userId = currentUserId)

            val documentReference = firestore.collection("diaryEntries")
                .add(entryWithUserId)
                .await()

            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDiaryEntry(entry: DiaryEntry): Result<Unit> {
        return try {
            firestore.collection("diaryEntries")
                .document(entry.entryId)
                .set(entry)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDiaryEntry(entryId: String): Result<Unit> {
        return try {
            firestore.collection("diaryEntries")
                .document(entryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDiaryEntryById(entryId: String): Result<DiaryEntry?> {
        return try {
            val document = firestore.collection("diaryEntries")
                .document(entryId)
                .get()
                .await()

            val entry = document.toObject(DiaryEntry::class.java)?.copy(entryId = document.id)
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val storageRef = storage.reference
                .child("diary-photos")
                .child(currentUserId)
                .child(fileName)

            val uploadResult = storageRef.putBytes(imageBytes).await()
            val downloadUrl = uploadResult.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadVoiceRecording(audioBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val storageRef = storage.reference
                .child("voice-memos")
                .child(currentUserId)
                .child(fileName)

            val uploadResult = storageRef.putBytes(audioBytes).await()
            val downloadUrl = uploadResult.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Template management
    fun getBuiltInTemplates(): Flow<List<Template>> = callbackFlow {
        val subscription = firestore.collection("templates")
            .whereEqualTo("isBuiltIn", true)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val templates = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Template::class.java)?.copy(templateId = document.id)
                } ?: emptyList()

                trySend(templates)
            }

        awaitClose { subscription.remove() }
    }

    fun getUserTemplates(): Flow<List<Template>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = firestore.collection("templates")
            .whereEqualTo("createdBy", currentUserId)
            .whereEqualTo("isBuiltIn", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val templates = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Template::class.java)?.copy(templateId = document.id)
                } ?: emptyList()

                trySend(templates)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createTemplate(template: Template): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val templateWithCreator = template.copy(createdBy = currentUserId, isBuiltIn = false)

            val documentReference = firestore.collection("templates")
                .add(templateWithCreator)
                .await()

            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchEntries(query: String): Result<List<DiaryEntry>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // For now, implement a simple text search
            // In production, you might want to use Algolia or Firebase's full-text search
            val documents = firestore.collection("diaryEntries")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            val entries = documents.documents.mapNotNull { document ->
                val entry = document.toObject(DiaryEntry::class.java)?.copy(entryId = document.id)
                entry?.takeIf { diaryEntry ->
                    diaryEntry.content.contains(query, ignoreCase = true) ||
                    diaryEntry.title?.contains(query, ignoreCase = true) == true ||
                    diaryEntry.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
            }

            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}