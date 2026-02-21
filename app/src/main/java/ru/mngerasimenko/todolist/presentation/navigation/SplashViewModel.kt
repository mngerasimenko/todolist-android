package ru.mngerasimenko.todolist.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.domain.model.ServerStatus
import ru.mngerasimenko.todolist.domain.repository.StatusRepository
import javax.inject.Inject

/**
 * ViewModel для проверки состояния авторизации и доступности сервера при запуске.
 * Определяет стартовый экран: Login, ListSelection или TodoList.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    tokenManager: TokenManager,
    private val statusRepository: StatusRepository
) : ViewModel() {

    /** Состояние авторизации: null — загрузка, true — авторизован, false — нет */
    val isLoggedIn: StateFlow<Boolean?> = tokenManager.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Есть ли сохранённый список: null — загрузка, true — есть, false — нет */
    val hasList: StateFlow<Boolean?> = tokenManager.listIdFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Loading)
    /** Статус доступности сервера */
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    init {
        checkServerStatus()
    }

    /** Проверка статуса сервера (вызывается при старте и по кнопке «Повторить») */
    fun checkServerStatus() {
        viewModelScope.launch {
            _serverStatus.value = ServerStatus.Loading
            _serverStatus.value = statusRepository.checkStatus()
        }
    }

    /** Сброс предупреждения о недоступности сервера (кнопка «Продолжить») */
    fun dismissServerWarning() {
        _serverStatus.value = ServerStatus.Ok
    }
}
