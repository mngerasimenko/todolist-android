package ru.mngerasimenko.todolist.data.repository

import android.util.Log
import kotlinx.serialization.json.Json
import ru.mngerasimenko.todolist.data.remote.ApiErrorParser
import ru.mngerasimenko.todolist.data.remote.api.AccountApiService
import ru.mngerasimenko.todolist.data.remote.dto.CreateAccountRequest
import ru.mngerasimenko.todolist.data.remote.dto.JoinAccountRequest
import ru.mngerasimenko.todolist.data.remote.dto.TodoResponse
import ru.mngerasimenko.todolist.domain.model.Account
import ru.mngerasimenko.todolist.domain.model.AccountMember
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo
import ru.mngerasimenko.todolist.domain.repository.AccountRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Реализация репозитория аккаунтов.
 * Вызывает Account API, маппит DTO → доменные модели.
 */
class AccountRepositoryImpl @Inject constructor(
    private val accountApi: AccountApiService,
    private val json: Json
) : AccountRepository {

    override suspend fun getMyAccounts(): Result<List<Account>> {
        return try {
            val response = accountApi.getMyAccounts()

            if (response.isSuccessful) {
                val accounts = response.body()?.map { dto ->
                    Account(
                        id = dto.id,
                        name = dto.name,
                        role = dto.role,
                        createdAt = dto.createdAt
                    )
                } ?: emptyList()
                Result.Success(accounts)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки аккаунтов", e)
            Result.Error("Ошибка загрузки аккаунтов: ${e.localizedMessage}")
        }
    }

    override suspend fun createAccount(name: String, password: String): Result<Account> {
        return try {
            val response = accountApi.createAccount(CreateAccountRequest(name, password))

            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")
                Result.Success(Account(dto.id, dto.name, dto.role, dto.createdAt))
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания аккаунта", e)
            Result.Error("Ошибка создания аккаунта: ${e.localizedMessage}")
        }
    }

    override suspend fun joinAccount(name: String, password: String): Result<Account> {
        return try {
            val response = accountApi.joinAccount(JoinAccountRequest(name, password))

            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")
                Result.Success(Account(dto.id, dto.name, dto.role, dto.createdAt))
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка вступления в аккаунт", e)
            Result.Error("Ошибка вступления в аккаунт: ${e.localizedMessage}")
        }
    }

    override suspend fun getMembers(accountId: Long): Result<List<AccountMember>> {
        return try {
            val response = accountApi.getMembers(accountId)

            if (response.isSuccessful) {
                val members = response.body()?.map { dto ->
                    AccountMember(
                        userId = dto.userId,
                        userName = dto.userName,
                        role = dto.role,
                        joinedAt = dto.joinedAt
                    )
                } ?: emptyList()
                Result.Success(members)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки участников", e)
            Result.Error("Ошибка загрузки участников: ${e.localizedMessage}")
        }
    }

    override suspend fun getTodosByAccount(accountId: Long): Result<List<Todo>> {
        return try {
            val response = accountApi.getTodosByAccount(accountId)

            if (response.isSuccessful) {
                val todos = response.body()?.map { dto -> mapTodo(dto) } ?: emptyList()
                Result.Success(todos)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки задач аккаунта", e)
            Result.Error("Ошибка загрузки задач: ${e.localizedMessage}")
        }
    }

    override suspend fun leaveAccount(accountId: Long): Result<Unit> {
        return try {
            val response = accountApi.leaveAccount(accountId)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка выхода из аккаунта", e)
            Result.Error("Ошибка выхода из аккаунта: ${e.localizedMessage}")
        }
    }

    /** Маппинг TodoResponse → Todo */
    private fun mapTodo(dto: TodoResponse): Todo = Todo(
        id = dto.id,
        name = dto.name,
        done = dto.done,
        isPrivate = dto.isPrivate,
        userId = dto.userId,
        userName = dto.userName,
        completorUserId = dto.completorUserId,
        completorUserName = dto.completorUserName,
        accountId = dto.accountId,
        creatorColor = dto.creatorColor,
        completorColor = dto.completorColor,
        createdAt = dto.createdAt,
        completedAt = dto.completedAt
    )

    companion object {
        private const val TAG = "AccountRepository"
    }
}
