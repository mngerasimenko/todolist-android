package ru.mngerasimenko.todolist.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.mngerasimenko.todolist.data.remote.dto.ListMemberResponse
import ru.mngerasimenko.todolist.data.remote.dto.ListResponse
import ru.mngerasimenko.todolist.data.remote.dto.CreateListRequest
import ru.mngerasimenko.todolist.data.remote.dto.JoinListRequest
import ru.mngerasimenko.todolist.data.remote.dto.TodoResponse

/** API списков задач */
interface ListApiService {

    /** Получить список списков задач текущего пользователя */
    @GET("api/lists")
    suspend fun getMyLists(): Response<List<ListResponse>>

    /** Создать новый список */
    @POST("api/lists")
    suspend fun createList(@Body request: CreateListRequest): Response<ListResponse>

    /** Вступить в существующий список */
    @POST("api/lists/join")
    suspend fun joinList(@Body request: JoinListRequest): Response<ListResponse>

    /** Получить участников списка */
    @GET("api/lists/{id}/members")
    suspend fun getMembers(@Path("id") id: Long): Response<List<ListMemberResponse>>

    /** Получить задачи списка (с учётом приватности) */
    @GET("api/lists/{id}/todos")
    suspend fun getTodosByList(@Path("id") id: Long): Response<List<TodoResponse>>

    /** Покинуть список */
    @DELETE("api/lists/{id}/leave")
    suspend fun leaveList(@Path("id") id: Long): Response<Unit>
}
