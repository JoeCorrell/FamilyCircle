package com.haven.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreManager @Inject constructor() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ── Haven (Family Circle) ──

    suspend fun createHaven(havenId: String, data: Map<String, Any>) {
        db.collection("havens").document(havenId).set(data, SetOptions.merge()).await()
    }

    suspend fun getHavenByInviteCode(code: String): String? {
        val snapshot = db.collection("havens")
            .whereEqualTo("inviteCode", code.uppercase())
            .limit(1)
            .get().await()
        return snapshot.documents.firstOrNull()?.id
    }

    suspend fun getHavenData(havenId: String): Map<String, Any>? {
        return db.collection("havens").document(havenId).get().await().data
    }

    // ── Members ──

    suspend fun setMember(havenId: String, userId: String, data: Map<String, Any?>) {
        db.collection("havens").document(havenId)
            .collection("members").document(userId)
            .set(data, SetOptions.merge()).await()
    }

    suspend fun updateMemberFields(havenId: String, userId: String, fields: Map<String, Any?>) {
        db.collection("havens").document(havenId)
            .collection("members").document(userId)
            .update(fields).await()
    }

    suspend fun removeMember(havenId: String, userId: String) {
        db.collection("havens").document(havenId)
            .collection("members").document(userId)
            .delete().await()
    }

    fun observeMembers(havenId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("havens").document(havenId).collection("members")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val members = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.plus("uid" to doc.id)
            } ?: emptyList()
            trySend(members)
        }
        awaitClose { listener.remove() }
    }

    // ── Messages ──

    suspend fun sendMessage(havenId: String, data: Map<String, Any>) {
        db.collection("havens").document(havenId)
            .collection("messages").add(data).await()
    }

    fun observeMessages(havenId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("havens").document(havenId)
            .collection("messages").orderBy("timestamp")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            } ?: emptyList()
            trySend(messages)
        }
        awaitClose { listener.remove() }
    }

    // ── Places ──

    suspend fun addPlace(havenId: String, data: Map<String, Any>): String {
        val doc = db.collection("havens").document(havenId)
            .collection("places").add(data).await()
        return doc.id
    }

    suspend fun removePlace(havenId: String, placeId: String) {
        db.collection("havens").document(havenId)
            .collection("places").document(placeId).delete().await()
    }

    fun observePlaces(havenId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("havens").document(havenId).collection("places")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val places = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            } ?: emptyList()
            trySend(places)
        }
        awaitClose { listener.remove() }
    }

    // ── Drives ──

    suspend fun addDrive(havenId: String, data: Map<String, Any>): String {
        val doc = db.collection("havens").document(havenId)
            .collection("drives").add(data).await()
        return doc.id
    }

    fun observeDrives(havenId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("havens").document(havenId)
            .collection("drives").orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val drives = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            } ?: emptyList()
            trySend(drives)
        }
        awaitClose { listener.remove() }
    }

    // ── Location History ──

    suspend fun addLocationHistory(havenId: String, userId: String, data: Map<String, Any>) {
        db.collection("havens").document(havenId)
            .collection("members").document(userId)
            .collection("history").add(data).await()
    }

    fun observeLocationHistory(havenId: String, userId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("havens").document(havenId)
            .collection("members").document(userId)
            .collection("history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val entries = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
            trySend(entries)
        }
        awaitClose { listener.remove() }
    }

    // ── SOS Alerts ──

    suspend fun sendSosAlert(havenId: String, data: Map<String, Any>) {
        db.collection("havens").document(havenId)
            .collection("alerts").add(data).await()
        // Also update haven-level flag so listeners pick it up immediately
        db.collection("havens").document(havenId)
            .update(mapOf(
                "activeSos" to true,
                "lastSosBy" to data["senderName"],
                "lastSosAt" to data["timestamp"]
            )).await()
    }

    suspend fun clearSosAlert(havenId: String) {
        db.collection("havens").document(havenId)
            .update(mapOf("activeSos" to false)).await()
    }

    fun observeHaven(havenId: String): Flow<Map<String, Any>?> = callbackFlow {
        val listener = db.collection("havens").document(havenId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(null); return@addSnapshotListener }
                trySend(snapshot?.data)
            }
        awaitClose { listener.remove() }
    }

    // ── Notifications ──

    suspend fun addNotification(havenId: String, data: Map<String, Any>) {
        db.collection("havens").document(havenId)
            .collection("notifications").add(data).await()
    }

    fun observeNotifications(havenId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("havens").document(havenId)
            .collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val notifs = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            } ?: emptyList()
            trySend(notifs)
        }
        awaitClose { listener.remove() }
    }

    // ── User Haven mapping ──

    suspend fun setUserHaven(userId: String, havenId: String) {
        db.collection("users").document(userId)
            .set(mapOf("havenId" to havenId), SetOptions.merge()).await()
    }

    suspend fun getUserHavenId(userId: String): String? {
        return db.collection("users").document(userId).get().await()
            .getString("havenId")
    }
}
