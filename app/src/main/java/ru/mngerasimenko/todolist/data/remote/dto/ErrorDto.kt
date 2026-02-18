package ru.mngerasimenko.todolist.data.remote.dto

import kotlinx.serialization.Serializable

/** Формат ошибки от бэкенда */
@Serializable
data class ErrorResponse(
    val timestamp: String? = null,
    val status: Int = 0,
    val error: String? = null,
    val message: String? = null
)
