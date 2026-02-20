package ru.mngerasimenko.todolist.data.repository

import android.util.Log
import kotlinx.serialization.json.Json
import ru.mngerasimenko.todolist.data.remote.ApiErrorParser
import ru.mngerasimenko.todolist.data.remote.api.TodoApiService
import ru.mngerasimenko.todolist.data.remote.dto.TodoRequest
import ru.mngerasimenko.todolist.data.remote.dto.TodoResponse
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo
import ru.mngerasimenko.todolist.domain.repository.TodoRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Реализация репозитория задач.
 * Вызывает Todo API, маппит TodoResponse → Todo.
 */
class TodoRepositoryImpl @Inject constructor(
    private val todoApi: TodoApiService,
    private val json: Json
) : TodoRepository {

    override suspend fun createTodo(
        name: String,
        userId: Long,
        accountId: Long,
        isPrivate: Boolean
    ): Result<Todo> {
        return try {
            val response = todoApi.createTodo(
                TodoRequest(
                    name = name,
                    userId = userId,
                    accountId = accountId,
                    isPrivate = isPrivate
                )
            )

            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")
                Result.Success(mapTodo(dto))
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания задачи", e)
            Result.Error("Ошибка создания задачи: ${e.localizedMessage}")
        }
    }

    override suspend fun updateTodo(
        id: Long,
        name: String,
        done: Boolean,
        userId: Long,
        accountId: Long
    ): Result<Todo> {
        return try {
            val response = todoApi.updateTodo(
                id,
                TodoRequest(
                    name = name,
                    userId = userId,
                    accountId = accountId,
                    done = done
                )
            )

            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")
                Result.Success(mapTodo(dto))
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка обновления задачи", e)
            Result.Error("Ошибка обновления задачи: ${e.localizedMessage}")
        }
    }

    override suspend fun deleteTodo(id: Long): Result<Unit> {
        return try {
            val response = todoApi.deleteTodo(id)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления задачи", e)
            Result.Error("Ошибка удаления задачи: ${e.localizedMessage}")
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
        private const val TAG = "TodoRepository"
    }
}
