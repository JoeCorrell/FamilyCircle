package com.haven.app.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haven.app.data.preferences.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    // TODO: Replace with your Railway URL
    private const val BASE_URL = "https://havenserver-production.up.railway.app/"
    // Your Railway server URL ^

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenStore: TokenStore): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val token = tokenStore.getToken()
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHavenApi(retrofit: Retrofit): HavenApi {
        return retrofit.create(HavenApi::class.java)
    }
}

@Singleton
class TokenStore @javax.inject.Inject constructor(
    private val userPreferences: UserPreferences
) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val HAVEN_ID_KEY = stringPreferencesKey("haven_id")
    }

    fun getToken(): String? = runBlocking {
        try { userPreferences.getString(TOKEN_KEY) } catch (_: Exception) { null }
    }

    fun getUserId(): String? = runBlocking {
        try { userPreferences.getString(USER_ID_KEY) } catch (_: Exception) { null }
    }

    fun getHavenId(): String? = runBlocking {
        try { userPreferences.getString(HAVEN_ID_KEY) } catch (_: Exception) { null }
    }

    suspend fun saveAuth(token: String, userId: String, havenId: String?) {
        userPreferences.setString(TOKEN_KEY, token)
        userPreferences.setString(USER_ID_KEY, userId)
        if (havenId != null) userPreferences.setString(HAVEN_ID_KEY, havenId)
    }

    suspend fun saveHavenId(havenId: String) {
        userPreferences.setString(HAVEN_ID_KEY, havenId)
    }

    suspend fun clear() {
        userPreferences.setString(TOKEN_KEY, "")
        userPreferences.setString(USER_ID_KEY, "")
        userPreferences.setString(HAVEN_ID_KEY, "")
    }
}
