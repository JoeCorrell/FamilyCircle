package com.haven.app.data.api

import retrofit2.Response
import retrofit2.http.*

// ── Request/Response models ──

data class AuthRequest(val email: String? = null, val phone: String? = null, val password: String)
data class AuthResponse(val token: String, val userId: String, val email: String?, val phone: String?, val havenId: String?)
data class MeResponse(val id: String, val email: String, val havenId: String?, val memberName: String?)

data class CreateHavenRequest(val name: String, val userName: String)
data class JoinHavenRequest(val inviteCode: String, val userName: String)
data class HavenResponse(val haven: HavenData, val member: MemberData)
data class HavenData(
    val id: String, val name: String, val inviteCode: String,
    val createdBy: String, val activeSos: Boolean
)

data class MemberData(
    val id: String = "", val userId: String = "", val havenId: String = "",
    val name: String = "", val initials: String = "", val color: Double = 0.0,
    val phoneNumber: String = "", val batteryLevel: Int = 100,
    val latitude: Double = 0.0, val longitude: Double = 0.0,
    val currentAddress: String = "", val speed: Double = 0.0,
    val status: String = "UNKNOWN", val lastSeenTimestamp: Double = 0.0,
    val isOnline: Boolean = false, val photoUrl: String = "",
    val avatarIcon: String = "", val ringPosition: Double = 0.5, val angle: Double = 0.0
) {
    fun colorAsLong(): Long = color.toLong()
    fun lastSeenAsLong(): Long = lastSeenTimestamp.toLong()
}

data class LocationUpdate(
    val latitude: Double, val longitude: Double,
    val speed: Double, val currentAddress: String,
    val status: String, val batteryLevel: Int
)

data class MessageData(
    val id: String, val havenId: String, val senderUid: String,
    val senderName: String, val text: String, val timestamp: Long
)

data class SendMessageRequest(val senderName: String, val text: String)

data class PlaceData(
    val id: String, val havenId: String, val name: String,
    val address: String, val latitude: Double, val longitude: Double,
    val radiusMeters: Float, val color: Long, val membersPresent: Int
)

data class CreatePlaceRequest(
    val name: String, val address: String,
    val latitude: Double, val longitude: Double,
    val radiusMeters: Float, val color: Long
)

data class NotificationData(
    val id: String, val havenId: String, val title: String,
    val color: Long, val timestamp: Long
)

data class CreateNotificationRequest(val title: String, val color: Long)

data class ErrandData(
    val id: String = "", val havenId: String = "", val senderUid: String = "",
    val senderName: String = "", val item: String = "", val address: String = "",
    val note: String = "", val status: String = "PENDING",
    val acceptedBy: String? = null, val acceptedName: String? = null,
    val timestamp: Double = 0.0
)

data class CreateErrandRequest(val senderName: String, val item: String, val address: String = "", val note: String = "")
data class AcceptErrandRequest(val acceptedName: String)

data class SosRequest(
    val senderName: String, val latitude: Double,
    val longitude: Double, val message: String
)

data class LocationHistoryEntry(
    val id: String, val address: String,
    val latitude: Double, val longitude: Double,
    val speed: Double, val status: String, val timestamp: Long
)

data class HavenDetailResponse(
    val id: String, val name: String, val inviteCode: String,
    val activeSos: Boolean, val members: List<MemberData>
)

// ── Retrofit API Interface ──

interface HavenApi {

    // Auth
    @POST("api/auth/signup")
    suspend fun signup(@Body body: AuthRequest): Response<AuthResponse>

    @POST("api/auth/signin")
    suspend fun signin(@Body body: AuthRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun me(): Response<MeResponse>

    // Havens
    @POST("api/havens")
    suspend fun createHaven(@Body body: CreateHavenRequest): Response<HavenResponse>

    @POST("api/havens/join")
    suspend fun joinHaven(@Body body: JoinHavenRequest): Response<HavenResponse>

    @GET("api/havens/{id}")
    suspend fun getHaven(@Path("id") id: String): Response<HavenDetailResponse>

    @PATCH("api/havens/{id}")
    suspend fun updateHaven(@Path("id") id: String, @Body body: Map<String, String>): Response<HavenData>

    // Members
    @GET("api/members/haven/{havenId}")
    suspend fun getMembers(@Path("havenId") havenId: String): Response<List<MemberData>>

    @GET("api/members/me")
    suspend fun getMyMember(): Response<MemberData>

    @PATCH("api/members/me")
    suspend fun updateMyMember(@Body fields: Map<String, @JvmSuppressWildcards Any>): Response<MemberData>

    @POST("api/members/location")
    suspend fun updateLocation(@Body body: LocationUpdate): Response<Map<String, Boolean>>

    @GET("api/members/{memberId}/history")
    suspend fun getLocationHistory(@Path("memberId") memberId: String): Response<List<LocationHistoryEntry>>

    // Messages
    @GET("api/messages/{havenId}")
    suspend fun getMessages(@Path("havenId") havenId: String): Response<List<MessageData>>

    @POST("api/messages/{havenId}")
    suspend fun sendMessage(@Path("havenId") havenId: String, @Body body: SendMessageRequest): Response<MessageData>

    // Places
    @GET("api/places/{havenId}")
    suspend fun getPlaces(@Path("havenId") havenId: String): Response<List<PlaceData>>

    @POST("api/places/{havenId}")
    suspend fun createPlace(@Path("havenId") havenId: String, @Body body: CreatePlaceRequest): Response<PlaceData>

    @DELETE("api/places/{havenId}/{placeId}")
    suspend fun deletePlace(@Path("havenId") havenId: String, @Path("placeId") placeId: String): Response<Map<String, Boolean>>

    // Notifications
    @GET("api/notifications/{havenId}")
    suspend fun getNotifications(@Path("havenId") havenId: String): Response<List<NotificationData>>

    @POST("api/notifications/{havenId}")
    suspend fun createNotification(@Path("havenId") havenId: String, @Body body: CreateNotificationRequest): Response<NotificationData>

    // Errands
    @GET("api/errands/{havenId}")
    suspend fun getErrands(@Path("havenId") havenId: String): Response<List<ErrandData>>

    @POST("api/errands/{havenId}")
    suspend fun createErrand(@Path("havenId") havenId: String, @Body body: CreateErrandRequest): Response<ErrandData>

    @POST("api/errands/{havenId}/{errandId}/accept")
    suspend fun acceptErrand(@Path("havenId") havenId: String, @Path("errandId") errandId: String, @Body body: AcceptErrandRequest): Response<ErrandData>

    // SOS
    @POST("api/sos/{havenId}")
    suspend fun activateSos(@Path("havenId") havenId: String, @Body body: SosRequest): Response<Map<String, Any>>

    @POST("api/sos/{havenId}/clear")
    suspend fun clearSos(@Path("havenId") havenId: String): Response<Map<String, Boolean>>
}
