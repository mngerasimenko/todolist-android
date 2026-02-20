package ru.mngerasimenko.todolist.domain.repository

import ru.mngerasimenko.todolist.domain.model.TaskList
import ru.mngerasimenko.todolist.domain.model.ListMember
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo

/** Интерфейс репозитория списков задач */
interface ListRepository {

    /** Получить списки задач текущего пользователя */
    suspend fun getMyLists(): Result<List<TaskList>>

    /** Создать новый список */
    suspend fun createList(name: String, password: String): Result<TaskList>

    /** Вступить в список */
    suspend fun joinList(name: String, password: String): Result<TaskList>

    /** Получить участников списка */
    suspend fun getMembers(listId: Long): Result<List<ListMember>>

    /** Получить задачи списка (с учётом приватности) */
    suspend fun getTodosByList(listId: Long): Result<List<Todo>>

    /** Покинуть список */
    suspend fun leaveList(listId: Long): Result<Unit>
}
