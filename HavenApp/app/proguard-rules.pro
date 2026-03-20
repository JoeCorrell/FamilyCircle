# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn dagger.hilt.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.haven.app.data.model.** { *; }
