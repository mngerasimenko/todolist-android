package ru.mngerasimenko.todolist.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// === Запросы ===

/** Запрос на создание аккаунта */
@Serializable
data class CreateAccountRequest(
    val name: String,
    val password: String
)

/** Запрос на вступление в аккаунт */
@Serializable
data class JoinAccountRequest(
    val name: String,
    val password: String
)

// === Ответы ===

/** Ответ с информацией об аккаунте */
@Serializable
data class AccountResponse(
    val id: Long,
    val name: String,
    val role: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

/** Ответ с информацией об участнике аккаунта */
@Serializable
data class AccountMemberResponse(
    @SerialName("user_id")
    val userId: Long,
    @SerialName("user_name")
    val userName: String,
    val role: String,
    @SerialName("joined_at")
    val joinedAt: String? = null
)
