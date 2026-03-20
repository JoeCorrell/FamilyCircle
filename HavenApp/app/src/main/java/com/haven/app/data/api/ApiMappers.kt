package com.haven.app.data.api

import com.haven.app.data.model.FamilyMember
import com.haven.app.data.model.MemberStatus

fun MemberData.toFamilyMember(): FamilyMember = FamilyMember(
    id = userId.hashCode().toLong(),
    name = name,
    initials = initials.ifEmpty { name.take(1).uppercase() },
    color = colorAsLong(),
    phoneNumber = phoneNumber,
    batteryLevel = batteryLevel,
    currentAddress = currentAddress,
    latitude = latitude,
    longitude = longitude,
    speed = speed.toFloat(),
    lastSeenTimestamp = lastSeenAsLong(),
    status = try { MemberStatus.valueOf(status) } catch (_: Exception) { MemberStatus.UNKNOWN },
    isOnline = isOnline,
    photoUrl = photoUrl,
    avatarIcon = avatarIcon,
    ringPosition = ringPosition.toFloat(),
    angle = angle.toFloat()
)
