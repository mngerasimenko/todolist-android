package ru.mngerasimenko.todolist.domain.model

sealed class ServerStatus {
    data object Loading : ServerStatus()
    data object Ok : ServerStatus()
    data object ServerUnavailable : ServerStatus()
    data object UpdateRequired : ServerStatus()
}
