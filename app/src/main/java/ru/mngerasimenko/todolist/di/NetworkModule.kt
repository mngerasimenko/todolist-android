package ru.mngerasimenko.todolist.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.mngerasimenko.todolist.BuildConfig
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.data.remote.api.AccountApiService
import ru.mngerasimenko.todolist.data.remote.api.AuthApiService
import ru.mngerasimenko.todolist.data.remote.api.TodoApiService
import ru.mngerasimenko.todolist.data.remote.interceptor.AuthInterceptor
import ru.mngerasimenko.todolist.data.remote.interceptor.TokenRefreshInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Hilt модуль для сетевого слоя.
 * Конфигурирует OkHttp, Retrofit и API сервисы.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenManager: TokenManager,
        authApiServiceProvider: Provider<AuthApiService>
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(TokenRefreshInterceptor(tokenManager, authApiServiceProvider))

        // Логирование HTTP запросов в debug режиме
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTodoApiService(retrofit: Retrofit): TodoApiService {
        return retrofit.create(TodoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAccountApiService(retrofit: Retrofit): AccountApiService {
        return retrofit.create(AccountApiService::class.java)
    }
}
