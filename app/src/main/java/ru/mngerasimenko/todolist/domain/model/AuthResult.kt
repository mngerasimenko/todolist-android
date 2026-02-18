package ru.mngerasimenko.todolist.domain.model

/**
 * Результат операции — обёртка для обработки успеха/ошибки.
 * Используется вместо исключений для предсказуемого потока данных.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = 0) : Result<Nothing>()
}
