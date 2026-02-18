package ru.mngerasimenko.todolist.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.mngerasimenko.todolist.data.local.TokenManager
import javax.inject.Inject

/**
 * ViewModel для проверки состояния авторизации при запуске приложения.
 * Если JWT токен сохранён — сразу переходим на экран задач,
 * иначе — на экран входа.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    tokenManager: TokenManager
) : ViewModel() {

    /** Состояние авторизации: null — загрузка, true — авторизован, false — нет */
    val isLoggedIn: StateFlow<Boolean?> = tokenManager.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
