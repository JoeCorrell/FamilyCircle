package com.haven.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadAvatar(userId: String, uri: Uri): String {
        val ref = storage.reference.child("avatars/$userId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
