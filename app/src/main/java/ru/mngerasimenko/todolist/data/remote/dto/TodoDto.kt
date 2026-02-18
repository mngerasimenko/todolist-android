package ru.mngerasimenko.todolist.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// === Запросы ===

@Serializable
data class TodoRequest(
    val name: String,
    @SerialName("user_id")
    val userId: Long,
    val done: Boolean = false
)

// === Ответы ===

@Serializable
data class TodoResponse(
    val id: Long,
    val name: String,
    @SerialName("date_time")
    val dateTime: String? = null,
    val done: Boolean,
    @SerialName("user_id")
    val userId: Long,
    @SerialName("user_name")
    val userName: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
