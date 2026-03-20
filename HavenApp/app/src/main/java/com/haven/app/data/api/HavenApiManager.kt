package com.haven.app.data.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager that replaces FirestoreManager + FirebaseAuthManager.
 * All data flows through the Haven REST API + polling for real-time updates.
 */
@Singleton
class HavenApiManager @Inject constructor(
    val api: HavenApi,
    private val tokenStore: TokenStore
) {
    // ── Auth ──

    val isSignedIn: Boolean get() = !tokenStore.getToken().isNullOrEmpty()
    val userId: String? get() = tokenStore.getUserId()
    val havenId: String? get() = tokenStore.getHavenId()

    suspend fun signUp(phone: String, password: String): Result<AuthResponse> {
        return try {
            val resp = api.signup(AuthRequest(phone = phone, password = password))
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                tokenStore.saveAuth(body.token, body.userId, body.havenId)
                Result.success(body)
            } else {
                val error = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                Result.failure(Exception(error ?: "Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(phone: String, password: String): Result<AuthResponse> {
        return try {
            val resp = api.signin(AuthRequest(phone = phone, password = password))
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                tokenStore.saveAuth(body.token, body.userId, body.havenId)
                Result.success(body)
            } else {
                val error = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                Result.failure(Exception(error ?: "Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        tokenStore.clear()
    }

    suspend fun checkAuth(): AuthState {
        val token = tokenStore.getToken()
        if (token.isNullOrEmpty()) return AuthState.SIGNED_OUT

        return try {
            val resp = api.me()
            if (resp.isSuccessful && resp.body() != null) {
                val me = resp.body()!!
                if (me.havenId != null) {
                    tokenStore.saveAuth(token, me.id, me.havenId)
                    AuthState.READY
                } else {
                    AuthState.NO_HAVEN
                }
            } else {
                tokenStore.clear()
                AuthState.SIGNED_OUT
            }
        } catch (e: Exception) {
            // Network error - check if we have cached haven ID
            if (tokenStore.getHavenId() != null) AuthState.READY
            else AuthState.SIGNED_OUT
        }
    }

    suspend fun getMyHavens(): List<HavenInfo> {
        return try {
            val resp = api.me()
            if (resp.isSuccessful) resp.body()?.havens ?: emptyList() else emptyList()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun switchHaven(havenId: String) {
        tokenStore.saveHavenId(havenId)
        // Reset SOS state so old alerts don't carry over
        _sosCleared = true
    }

    @Volatile
    var _sosCleared = false

    // SOS received state — observed by UI to show full-screen alert
    data class SosAlert(val senderName: String, val latitude: Double, val longitude: Double)
    val sosReceived = MutableStateFlow<SosAlert?>(null)

    fun dismissSosAlert() {
        sosReceived.value = null
    }

    // ── Haven ──

    suspend fun createHaven(name: String, userName: String): Result<HavenResponse> {
        return try {
            val resp = api.createHaven(CreateHavenRequest(name, userName))
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                tokenStore.saveHavenId(body.haven.id)
                Result.success(body)
            } else {
                Result.failure(Exception(resp.errorBody()?.string() ?: "Failed to create circle"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinHaven(inviteCode: String, userName: String): Result<HavenResponse> {
        return try {
            val resp = api.joinHaven(JoinHavenRequest(inviteCode, userName))
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                tokenStore.saveHavenId(body.haven.id)
                Result.success(body)
            } else {
                Result.failure(Exception(resp.errorBody()?.string() ?: "Invalid invite code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHaven(): HavenDetailResponse? {
        val hid = tokenStore.getHavenId() ?: return null
        return try {
            val resp = api.getHaven(hid)
            resp.body()
        } catch (_: Exception) { null }
    }

    suspend fun checkSosActive(): Pair<Boolean, String?> {
        return try {
            val hid = tokenStore.getHavenId() ?: return false to null
            val resp = api.getHaven(hid)
            val haven = resp.body() ?: return false to null
            haven.activeSos to haven.lastSosBy
        } catch (_: Exception) { false to null }
    }

    suspend fun updateHavenName(name: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.updateHaven(hid, mapOf("name" to name)) } catch (_: Exception) {}
    }

    suspend fun kickMember(memberId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.kickMember(hid, memberId) } catch (_: Exception) {}
    }

    suspend fun promoteMember(memberId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.promoteMember(hid, memberId) } catch (_: Exception) {}
    }

    suspend fun demoteMember(memberId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.demoteMember(hid, memberId) } catch (_: Exception) {}
    }

    // ── Members (polling-based flow) ──

    fun observeMembers(): Flow<List<MemberData>> = flow {
        var firstLoad = true
        while (true) {
            val hid = tokenStore.getHavenId()
            if (hid != null) {
                try {
                    val resp = api.getMembers(hid)
                    if (resp.isSuccessful) {
                        emit(resp.body() ?: emptyList())
                        firstLoad = false
                    }
                } catch (_: Exception) {}
            }
            delay(if (firstLoad) 1000 else 5000)
        }
    }.distinctUntilChanged()

    suspend fun getMyMember(): MemberData? {
        return try { api.getMyMember().body() } catch (_: Exception) { null }
    }

    suspend fun updateMyMember(fields: Map<String, Any>) {
        try { api.updateMyMember(fields) } catch (_: Exception) {}
    }

    suspend fun updateLocation(update: LocationUpdate) {
        try { api.updateLocation(update) } catch (_: Exception) {}
    }

    suspend fun getLocationHistory(memberId: String): List<LocationHistoryEntry> {
        return try { api.getLocationHistory(memberId).body() ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    // ── Messages (polling-based flow) ──

    fun observeMessages(): Flow<List<MessageData>> = flow {
        var firstLoad = true
        while (true) {
            val hid = tokenStore.getHavenId()
            if (hid != null) {
                try {
                    val resp = api.getMessages(hid)
                    if (resp.isSuccessful) { emit(resp.body() ?: emptyList()); firstLoad = false }
                } catch (_: Exception) {}
            }
            delay(if (firstLoad) 1000 else 3000)
        }
    }.distinctUntilChanged()

    suspend fun sendMessage(senderName: String, text: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.sendMessage(hid, SendMessageRequest(senderName, text)) } catch (_: Exception) {}
    }

    // ── Places (polling-based flow) ──

    fun observePlaces(): Flow<List<PlaceData>> = flow {
        var firstLoad = true
        while (true) {
            val hid = tokenStore.getHavenId()
            if (hid != null) {
                try {
                    val resp = api.getPlaces(hid)
                    if (resp.isSuccessful) { emit(resp.body() ?: emptyList()); firstLoad = false }
                } catch (_: Exception) {}
            }
            delay(if (firstLoad) 1000 else 10000)
        }
    }.distinctUntilChanged()

    suspend fun createPlace(request: CreatePlaceRequest) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createPlace(hid, request) } catch (_: Exception) {}
    }

    suspend fun deletePlace(placeId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.deletePlace(hid, placeId) } catch (_: Exception) {}
    }

    // ── Notifications (polling-based flow) ──

    fun observeNotifications(): Flow<List<NotificationData>> = flow {
        var firstLoad = true
        while (true) {
            val hid = tokenStore.getHavenId()
            if (hid != null) {
                try {
                    val resp = api.getNotifications(hid)
                    if (resp.isSuccessful) { emit(resp.body() ?: emptyList()); firstLoad = false }
                } catch (_: Exception) {}
            }
            delay(if (firstLoad) 1000 else 15000)
        }
    }.distinctUntilChanged()

    suspend fun createNotification(title: String, color: Long) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createNotification(hid, CreateNotificationRequest(title, color)) } catch (_: Exception) {}
    }

    // ── Errands ──

    fun observeErrands(): Flow<List<ErrandData>> = flow {
        var firstLoad = true
        while (true) {
            val hid = tokenStore.getHavenId()
            if (hid != null) {
                try {
                    val resp = api.getErrands(hid)
                    if (resp.isSuccessful) { emit(resp.body() ?: emptyList()); firstLoad = false }
                } catch (_: Exception) {}
            }
            delay(if (firstLoad) 1000 else 5000)
        }
    }.distinctUntilChanged()

    suspend fun createErrand(senderName: String, item: String, address: String = "", note: String = "") {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createErrand(hid, CreateErrandRequest(senderName, item, address, note)) } catch (_: Exception) {}
    }

    suspend fun acceptErrand(errandId: String, acceptedName: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.acceptErrand(hid, errandId, AcceptErrandRequest(acceptedName)) } catch (_: Exception) {}
    }

    // ── SOS ──

    suspend fun activateSos(senderName: String, lat: Double, lng: Double) {
        val hid = tokenStore.getHavenId() ?: return
        try {
            api.activateSos(hid, SosRequest(senderName, lat, lng, "SOS ALERT! I need help!"))
        } catch (_: Exception) {}
    }

    suspend fun clearSos() {
        val hid = tokenStore.getHavenId() ?: return
        try { api.clearSos(hid) } catch (_: Exception) {}
    }
}

enum class AuthState { LOADING, SIGNED_OUT, NO_HAVEN, READY }
