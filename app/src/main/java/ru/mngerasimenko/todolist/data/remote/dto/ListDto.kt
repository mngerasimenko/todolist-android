package ru.mngerasimenko.todolist.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// === Запросы ===

/** Запрос на создание списка */
@Serializable
data class CreateListRequest(
    val name: String,
    val password: String
)

/** Запрос на вступление в список */
@Serializable
data class JoinListRequest(
    val name: String,
    val password: String
)

// === Ответы ===

/** Ответ с информацией о списке */
@Serializable
data class ListResponse(
    val id: Long,
    val name: String,
    val role: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

/** Ответ с информацией об участнике списка */
@Serializable
data class ListMemberResponse(
    @SerialName("user_id")
    val userId: Long,
    @SerialName("user_name")
    val userName: String,
    val role: String,
    @SerialName("joined_at")
    val joinedAt: String? = null
)
