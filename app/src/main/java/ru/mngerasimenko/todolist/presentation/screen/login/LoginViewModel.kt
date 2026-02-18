package ru.mngerasimenko.todolist.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.repository.AuthRepository
import javax.inject.Inject

/** Состояние экрана входа */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

/**
 * ViewModel для экрана входа.
 * Управляет состоянием формы и вызывает AuthRepository для аутентификации.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Обновление поля "имя пользователя" */
    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    /** Обновление поля "пароль" */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    /** Выполнить вход */
    fun login() {
        val state = _uiState.value

        // Валидация полей
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите имя пользователя") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите пароль") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.login(state.username, state.password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /** Сброс флага успешного входа (после навигации) */
    fun onLoginHandled() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }
}
