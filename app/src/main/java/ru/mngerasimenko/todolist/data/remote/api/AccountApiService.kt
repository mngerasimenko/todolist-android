package ru.mngerasimenko.todolist.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.mngerasimenko.todolist.data.remote.dto.AccountMemberResponse
import ru.mngerasimenko.todolist.data.remote.dto.AccountResponse
import ru.mngerasimenko.todolist.data.remote.dto.CreateAccountRequest
import ru.mngerasimenko.todolist.data.remote.dto.JoinAccountRequest
import ru.mngerasimenko.todolist.data.remote.dto.TodoResponse

/** API аккаунтов */
interface AccountApiService {

    /** Получить список аккаунтов текущего пользователя */
    @GET("api/accounts")
    suspend fun getMyAccounts(): Response<List<AccountResponse>>

    /** Создать новый аккаунт */
    @POST("api/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): Response<AccountResponse>

    /** Вступить в существующий аккаунт */
    @POST("api/accounts/join")
    suspend fun joinAccount(@Body request: JoinAccountRequest): Response<AccountResponse>

    /** Получить участников аккаунта */
    @GET("api/accounts/{id}/members")
    suspend fun getMembers(@Path("id") id: Long): Response<List<AccountMemberResponse>>

    /** Получить задачи аккаунта (с учётом приватности) */
    @GET("api/accounts/{id}/todos")
    suspend fun getTodosByAccount(@Path("id") id: Long): Response<List<TodoResponse>>

    /** Покинуть аккаунт */
    @DELETE("api/accounts/{id}/leave")
    suspend fun leaveAccount(@Path("id") id: Long): Response<Unit>
}
