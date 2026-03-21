package com.haven.app.di

import android.content.Context
import androidx.room.Room
import com.haven.app.data.local.HavenDatabase
import com.haven.app.data.local.dao.EmergencyContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HavenDatabase {
        return Room.databaseBuilder(
            context,
            HavenDatabase::class.java,
            "haven_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideEmergencyContactDao(db: HavenDatabase): EmergencyContactDao = db.emergencyContactDao()
}
