package com.haven.app.data.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
        sosReceived.value = null  // Clear any active SOS alert when switching havens
    }

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

    private fun <T> pollingFlow(
        intervalMs: Long,
        fetch: suspend (havenId: String) -> retrofit2.Response<List<T>>
    ): Flow<List<T>> = flow {
        var firstLoad = true
        while (true) {
            val hid = tokenStore.getHavenId()
            if (hid != null) {
                try {
                    val resp = fetch(hid)
                    if (resp.isSuccessful) { emit(resp.body() ?: emptyList()); firstLoad = false }
                } catch (_: Exception) {}
            }
            delay(if (firstLoad) 1000L else intervalMs)
        }
    }.distinctUntilChanged()

    private val _members = pollingFlow(5000) { api.getMembers(it) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    fun observeMembers(): Flow<List<MemberData>> = _members

    fun observeMyMember(): Flow<MemberData?> = _members
        .map { members -> members.firstOrNull { it.userId == userId } }

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

    private val _messages = pollingFlow(3000) { api.getMessages(it) }
        .shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun observeMessages(): Flow<List<MessageData>> = _messages

    suspend fun sendMessage(senderName: String, text: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.sendMessage(hid, SendMessageRequest(senderName, text)) } catch (_: Exception) {}
    }

    // ── Places (polling-based flow) ──

    private val _places = pollingFlow(10000) { api.getPlaces(it) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    fun observePlaces(): Flow<List<PlaceData>> = _places

    suspend fun createPlace(request: CreatePlaceRequest) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createPlace(hid, request) } catch (_: Exception) {}
    }

    suspend fun deletePlace(placeId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.deletePlace(hid, placeId) } catch (_: Exception) {}
    }

    // ── Notifications (polling-based flow) ──

    private val _notifications = pollingFlow(15000) { api.getNotifications(it) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    fun observeNotifications(): Flow<List<NotificationData>> = _notifications

    suspend fun createNotification(title: String, color: Long) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createNotification(hid, CreateNotificationRequest(title, color)) } catch (_: Exception) {}
    }

    // ── Errands ──

    private val _errands = pollingFlow(5000) { api.getErrands(it) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    fun observeErrands(): Flow<List<ErrandData>> = _errands

    suspend fun createErrand(senderName: String, item: String, address: String = "", note: String = "") {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createErrand(hid, CreateErrandRequest(senderName, item, address, note)) } catch (_: Exception) {}
    }

    suspend fun acceptErrand(errandId: String, acceptedName: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.acceptErrand(hid, errandId, AcceptErrandRequest(acceptedName)) } catch (_: Exception) {}
    }

    suspend fun completeErrand(errandId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.completeErrand(hid, errandId) } catch (_: Exception) {}
    }

    suspend fun declineErrand(errandId: String) {
        val hid = tokenStore.getHavenId() ?: return
        try { api.declineErrand(hid, errandId) } catch (_: Exception) {}
    }

    // ── Drives (polling-based flow) ──

    private val _drives = pollingFlow(10000) { api.getDrives(it) }
        .shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun observeDrives(): Flow<List<DriveData>> = _drives

    // ── Check-ins (polling-based flow) ──

    private val _checkins = pollingFlow(10000) { api.getCheckins(it) }
        .shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    fun observeCheckins(): Flow<List<CheckinData>> = _checkins

    suspend fun createCheckin(senderName: String, emoji: String, message: String = "") {
        val hid = tokenStore.getHavenId() ?: return
        try { api.createCheckin(hid, CreateCheckinRequest(senderName, emoji, message)) } catch (_: Exception) {}
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
