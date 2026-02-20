package ru.mngerasimenko.todolist.domain.model

/** Модель задачи (доменный слой) */
data class Todo(
    val id: Long,
    val name: String,
    val done: Boolean,
    val isPrivate: Boolean = false,
    val userId: Long,
    val userName: String?,
    val completorUserId: Long? = null,
    val completorUserName: String? = null,
    val accountId: Long? = null,
    val creatorColor: String? = null,
    val completorColor: String? = null,
    val createdAt: String?,
    val completedAt: String? = null
)
