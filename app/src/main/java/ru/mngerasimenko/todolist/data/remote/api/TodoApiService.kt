package ru.mngerasimenko.todolist.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.mngerasimenko.todolist.data.remote.dto.TodoRequest
import ru.mngerasimenko.todolist.data.remote.dto.TodoResponse

/** API задач */
interface TodoApiService {

    @GET("api/todos/user/{userId}")
    suspend fun getTodosByUserId(@Path("userId") userId: Long): Response<List<TodoResponse>>

    @GET("api/todos/{id}")
    suspend fun getTodoById(@Path("id") id: Long): Response<TodoResponse>

    @POST("api/todos/create")
    suspend fun createTodo(@Body request: TodoRequest): Response<TodoResponse>

    @PUT("api/todos/{id}")
    suspend fun updateTodo(
        @Path("id") id: Long,
        @Body request: TodoRequest
    ): Response<TodoResponse>

    @DELETE("api/todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Long): Response<Unit>
}
