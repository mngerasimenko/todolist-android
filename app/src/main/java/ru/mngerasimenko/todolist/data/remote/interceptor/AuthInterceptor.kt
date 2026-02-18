package ru.mngerasimenko.todolist.data.remote.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.mngerasimenko.todolist.data.local.TokenManager
import javax.inject.Inject

/**
 * OkHttp перехватчик для автоматического добавления JWT токена
 * в заголовок Authorization для всех запросов к защищённым эндпоинтам.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        // Пути, не требующие авторизации
        private val PUBLIC_PATHS = listOf(
            "api/auth/login",
            "api/auth/register",
            "api/auth/refresh",
            "api/status",
            "api/appName"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.encodedPath

        // Публичные эндпоинты не требуют токена
        if (PUBLIC_PATHS.any { url.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        // Получаем токен из DataStore
        val token = runBlocking {
            tokenManager.accessTokenFlow.first()
        }

        // Если токен есть — добавляем в заголовок
        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
