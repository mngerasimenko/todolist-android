package ru.mngerasimenko.todolist.domain.repository

import ru.mngerasimenko.todolist.domain.model.ServerStatus

interface StatusRepository {
    suspend fun checkStatus(): ServerStatus
}
