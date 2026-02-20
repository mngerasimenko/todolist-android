package ru.mngerasimenko.todolist.domain.repository

import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo

/** Интерфейс репозитория задач */
interface TodoRepository {

    /** Создать новую задачу */
    suspend fun createTodo(name: String, userId: Long, accountId: Long, isPrivate: Boolean = false): Result<Todo>

    /** Обновить задачу (имя или статус) */
    suspend fun updateTodo(id: Long, name: String, done: Boolean, userId: Long, accountId: Long): Result<Todo>

    /** Удалить задачу */
    suspend fun deleteTodo(id: Long): Result<Unit>
}
