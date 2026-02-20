package ru.mngerasimenko.todolist.domain.repository

import ru.mngerasimenko.todolist.domain.model.Account
import ru.mngerasimenko.todolist.domain.model.AccountMember
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo

/** Интерфейс репозитория аккаунтов */
interface AccountRepository {

    /** Получить список аккаунтов текущего пользователя */
    suspend fun getMyAccounts(): Result<List<Account>>

    /** Создать новый аккаунт */
    suspend fun createAccount(name: String, password: String): Result<Account>

    /** Вступить в аккаунт */
    suspend fun joinAccount(name: String, password: String): Result<Account>

    /** Получить участников аккаунта */
    suspend fun getMembers(accountId: Long): Result<List<AccountMember>>

    /** Получить задачи аккаунта (с учётом приватности) */
    suspend fun getTodosByAccount(accountId: Long): Result<List<Todo>>

    /** Покинуть аккаунт */
    suspend fun leaveAccount(accountId: Long): Result<Unit>
}
