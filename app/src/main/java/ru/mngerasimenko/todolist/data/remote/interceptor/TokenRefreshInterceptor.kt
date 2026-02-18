package ru.mngerasimenko.todolist.data.remote.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.data.remote.api.AuthApiService
import ru.mngerasimenko.todolist.data.remote.dto.RefreshTokenRequest
import javax.inject.Inject
import javax.inject.Provider

/**
 * Перехватчик для автоматического обновления JWT токена при получении 401.
 * Использует refresh token для получения нового access token.
 */
class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiServiceProvider: Provider<AuthApiService>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // Если ответ 401 — пробуем обновить токен
        if (response.code == 401) {
            val refreshToken = runBlocking {
                tokenManager.refreshTokenFlow.first()
            }

            if (!refreshToken.isNullOrBlank()) {
                // Закрываем старый ответ
                response.close()

                // Пробуем обновить токен
                val refreshResponse = runBlocking {
                    try {
                        val authApi = authApiServiceProvider.get()
                        val result = authApi.refreshToken(RefreshTokenRequest(refreshToken))
                        if (result.isSuccessful) {
                            result.body()?.let { loginResponse ->
                                tokenManager.saveTokens(
                                    accessToken = loginResponse.accessToken,
                                    refreshToken = loginResponse.refreshToken
                                )
                                loginResponse.accessToken
                            }
                        } else {
                            // Refresh token невалиден — очищаем
                            tokenManager.clearAll()
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                // Повторяем оригинальный запрос с новым токеном
                if (refreshResponse != null) {
                    val newRequest = chain.request().newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $refreshResponse")
                        .build()
                    return chain.proceed(newRequest)
                }
            }
        }

        return response
    }
}
