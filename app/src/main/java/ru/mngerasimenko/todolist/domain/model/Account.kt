package ru.mngerasimenko.todolist.domain.model

/** Модель аккаунта (доменный слой) */
data class Account(
    val id: Long,
    val name: String,
    val role: String,
    val createdAt: String?
)

/** Модель участника аккаунта (доменный слой) */
data class AccountMember(
    val userId: Long,
    val userName: String,
    val role: String,
    val joinedAt: String?
)
