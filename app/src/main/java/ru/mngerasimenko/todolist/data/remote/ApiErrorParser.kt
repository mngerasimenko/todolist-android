package ru.mngerasimenko.todolist.data.remote

import kotlinx.serialization.json.Json
import ru.mngerasimenko.todolist.data.remote.dto.ErrorResponse

/**
 * Утилита для парсинга ошибок из JSON ответа бэкенда.
 * Общий код для всех Repository.
 */
object ApiErrorParser {

    /** Парсит JSON-ошибку из тела ответа */
    fun parse(json: Json, errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Неизвестная ошибка"
        return try {
            val error = json.decodeFromString<ErrorResponse>(errorBody)
            error.message ?: error.error ?: "Неизвестная ошибка"
        } catch (e: Exception) {
            errorBody
        }
    }
}
