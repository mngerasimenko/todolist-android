package ru.mngerasimenko.todolist.data.repository

import android.util.Log
import kotlinx.serialization.json.Json
import ru.mngerasimenko.todolist.data.remote.ApiErrorParser
import ru.mngerasimenko.todolist.data.remote.api.ListApiService
import ru.mngerasimenko.todolist.data.remote.dto.CreateListRequest
import ru.mngerasimenko.todolist.data.remote.dto.JoinListRequest
import ru.mngerasimenko.todolist.data.remote.dto.TodoResponse
import ru.mngerasimenko.todolist.domain.model.TaskList
import ru.mngerasimenko.todolist.domain.model.ListMember
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo
import ru.mngerasimenko.todolist.domain.repository.ListRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Реализация репозитория списков задач.
 * Вызывает List API, маппит DTO → доменные модели.
 */
class ListRepositoryImpl @Inject constructor(
    private val listApi: ListApiService,
    private val json: Json
) : ListRepository {

    override suspend fun getMyLists(): Result<List<TaskList>> {
        return try {
            val response = listApi.getMyLists()

            if (response.isSuccessful) {
                val lists = response.body()?.map { dto ->
                    TaskList(
                        id = dto.id,
                        name = dto.name,
                        role = dto.role,
                        createdAt = dto.createdAt
                    )
                } ?: emptyList()
                Result.Success(lists)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки списков", e)
            Result.Error("Ошибка загрузки списков: ${e.localizedMessage}")
        }
    }

    override suspend fun createList(name: String, password: String): Result<TaskList> {
        return try {
            val response = listApi.createList(CreateListRequest(name, password))

            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")
                Result.Success(TaskList(dto.id, dto.name, dto.role, dto.createdAt))
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания списка", e)
            Result.Error("Ошибка создания списка: ${e.localizedMessage}")
        }
    }

    override suspend fun joinList(name: String, password: String): Result<TaskList> {
        return try {
            val response = listApi.joinList(JoinListRequest(name, password))

            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return Result.Error("Пустой ответ от сервера")
                Result.Success(TaskList(dto.id, dto.name, dto.role, dto.createdAt))
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка вступления в список", e)
            Result.Error("Ошибка вступления в список: ${e.localizedMessage}")
        }
    }

    override suspend fun getMembers(listId: Long): Result<List<ListMember>> {
        return try {
            val response = listApi.getMembers(listId)

            if (response.isSuccessful) {
                val members = response.body()?.map { dto ->
                    ListMember(
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

    override suspend fun getTodosByList(listId: Long): Result<List<Todo>> {
        return try {
            val response = listApi.getTodosByList(listId)

            if (response.isSuccessful) {
                val todos = response.body()?.map { dto -> mapTodo(dto) } ?: emptyList()
                Result.Success(todos)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки задач списка", e)
            Result.Error("Ошибка загрузки задач: ${e.localizedMessage}")
        }
    }

    override suspend fun leaveList(listId: Long): Result<Unit> {
        return try {
            val response = listApi.leaveList(listId)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiErrorParser.parse(json, response.errorBody()?.string()), response.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка выхода из списка", e)
            Result.Error("Ошибка выхода из списка: ${e.localizedMessage}")
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
        listId = dto.listId,
        creatorColor = dto.creatorColor,
        completorColor = dto.completorColor,
        createdAt = dto.createdAt,
        completedAt = dto.completedAt
    )

    companion object {
        private const val TAG = "ListRepository"
    }
}
