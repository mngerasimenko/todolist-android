package ru.mngerasimenko.todolist.data.repository

import android.util.Log
import kotlinx.serialization.json.Json
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.data.remote.ApiErrorParser
import ru.mngerasimenko.todolist.data.remote.api.AuthApiService
import ru.mngerasimenko.todolist.data.remote.dto.LoginRequest
import ru.mngerasimenko.todolist.data.remote.dto.RegisterRequest
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.User
import ru.mngerasimenko.todolist.domain.repository.AuthRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Реализация репозитория аутентификации.
 * Вызывает Auth API, сохраняет JWT токены в TokenManager,
 * маппит DTO → доменные модели.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager,
    private val json: Json
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(username, password))

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")

                // Сохраняем токены и данные пользователя
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                tokenManager.saveUser(body.user.id, body.user.name, body.user.email)

                Result.Success(User(body.user.id, body.user.email, body.user.name))
            } else {
                val errorMessage = ApiErrorParser.parse(json, response.errorBody()?.string())
                Result.Error(errorMessage, response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка входа", e)
            Result.Error("Ошибка подключения: ${e.localizedMessage}")
        }
    }

    override suspend fun register(email: String, name: String, password: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(email, name, password))

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")

                // Сохраняем токены и данные пользователя
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                tokenManager.saveUser(body.user.id, body.user.name, body.user.email)

                Result.Success(User(body.user.id, body.user.email, body.user.name))
            } else {
                val errorMessage = ApiErrorParser.parse(json, response.errorBody()?.string())
                Result.Error(errorMessage, response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации", e)
            Result.Error("Ошибка подключения: ${e.localizedMessage}")
        }
    }

    override suspend fun logout() {
        tokenManager.clearAll()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
