package ru.mngerasimenko.todolist.domain.repository

import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo

/** Интерфейс репозитория задач */
interface TodoRepository {

    /** Получить все задачи пользователя */
    suspend fun getTodosByUserId(userId: Long): Result<List<Todo>>

    /** Создать новую задачу */
    suspend fun createTodo(name: String, userId: Long): Result<Todo>

    /** Обновить задачу (имя или статус) */
    suspend fun updateTodo(id: Long, name: String, done: Boolean, userId: Long): Result<Todo>

    /** Удалить задачу */
    suspend fun deleteTodo(id: Long): Result<Unit>
}
