package com.haven.app.data.local

import androidx.room.TypeConverter
import com.haven.app.data.model.MemberStatus
import com.haven.app.data.model.NotificationType

class Converters {
    @TypeConverter
    fun fromMemberStatus(status: MemberStatus): String = status.name

    @TypeConverter
    fun toMemberStatus(value: String): MemberStatus =
        try { MemberStatus.valueOf(value) } catch (_: Exception) { MemberStatus.UNKNOWN }

    @TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType =
        try { NotificationType.valueOf(value) } catch (_: Exception) { NotificationType.INFO }
}
