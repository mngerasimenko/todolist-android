package ru.mngerasimenko.todolist.domain.model

/** Модель пользователя (доменный слой) */
data class User(
    val id: Long,
    val email: String,
    val name: String
)
