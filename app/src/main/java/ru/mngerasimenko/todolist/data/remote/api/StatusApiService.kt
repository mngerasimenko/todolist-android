package ru.mngerasimenko.todolist.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import ru.mngerasimenko.todolist.data.remote.dto.StatusResponse

interface StatusApiService {

    @GET("api/status")
    suspend fun getStatus(): Response<StatusResponse>
}
