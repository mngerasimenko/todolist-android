package ru.mngerasimenko.todolist.domain.repository

import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.User

/** Интерфейс репозитория аутентификации */
interface AuthRepository {

    /** Вход пользователя */
    suspend fun login(username: String, password: String): Result<User>

    /** Регистрация нового пользователя */
    suspend fun register(email: String, name: String, password: String): Result<User>

    /** Выход из системы (очистка токенов) */
    suspend fun logout()
}
