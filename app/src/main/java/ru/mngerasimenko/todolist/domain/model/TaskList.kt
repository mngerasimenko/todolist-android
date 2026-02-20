package ru.mngerasimenko.todolist.domain.model

/** Модель списка задач (доменный слой) */
data class TaskList(
    val id: Long,
    val name: String,
    val role: String,
    val createdAt: String?
)

/** Модель участника списка (доменный слой) */
data class ListMember(
    val userId: Long,
    val userName: String,
    val role: String,
    val joinedAt: String?
)
