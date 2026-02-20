package ru.mngerasimenko.todolist.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// === Запросы ===

@Serializable
data class TodoRequest(
    val name: String,
    @SerialName("user_id")
    val userId: Long,
    @SerialName("account_id")
    val accountId: Long,
    val done: Boolean = false,
    @SerialName("is_private")
    val isPrivate: Boolean = false
)

// === Ответы ===

@Serializable
data class TodoResponse(
    val id: Long,
    val name: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    val done: Boolean,
    @SerialName("is_private")
    val isPrivate: Boolean = false,
    @SerialName("user_id")
    val userId: Long,
    @SerialName("user_name")
    val userName: String? = null,
    @SerialName("completor_user_id")
    val completorUserId: Long? = null,
    @SerialName("completor_user_name")
    val completorUserName: String? = null,
    @SerialName("account_id")
    val accountId: Long? = null,
    @SerialName("creator_color")
    val creatorColor: String? = null,
    @SerialName("completor_color")
    val completorColor: String? = null
)
