package ru.mngerasimenko.todolist.domain.model

/** Модель задачи (доменный слой) */
data class Todo(
    val id: Long,
    val name: String,
    val done: Boolean,
    val userId: Long,
    val userName: String?,
    val createdAt: String?
)
