package ru.mngerasimenko.todolist.data.repository

import android.util.Log
import ru.mngerasimenko.todolist.BuildConfig
import ru.mngerasimenko.todolist.data.remote.api.StatusApiService
import ru.mngerasimenko.todolist.domain.model.ServerStatus
import ru.mngerasimenko.todolist.domain.repository.StatusRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class StatusRepositoryImpl @Inject constructor(
    private val statusApi: StatusApiService
) : StatusRepository {

    override suspend fun checkStatus(): ServerStatus {
        return try {
            val response = statusApi.getStatus()

            if (response.isSuccessful) {
                val body = response.body() ?: return ServerStatus.ServerUnavailable
                val minVersion = body.minAndroidVersion ?: return ServerStatus.Ok

                if (BuildConfig.VERSION_CODE < minVersion) {
                    ServerStatus.UpdateRequired
                } else {
                    ServerStatus.Ok
                }
            } else {
                ServerStatus.ServerUnavailable
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Server status check failed", e)
            ServerStatus.ServerUnavailable
        }
    }

    companion object {
        private const val TAG = "StatusRepository"
    }
}
