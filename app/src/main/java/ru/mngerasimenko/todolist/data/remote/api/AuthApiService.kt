package ru.mngerasimenko.todolist.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.mngerasimenko.todolist.data.remote.dto.LoginRequest
import ru.mngerasimenko.todolist.data.remote.dto.LoginResponse
import ru.mngerasimenko.todolist.data.remote.dto.RefreshTokenRequest
import ru.mngerasimenko.todolist.data.remote.dto.RegisterRequest

/** API аутентификации */
interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<LoginResponse>
}
