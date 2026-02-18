package ru.mngerasimenko.todolist.presentation.screen.register

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

/** Состояние экрана регистрации */
data class RegisterUiState(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterSuccess: Boolean = false
)

/**
 * ViewModel для экрана регистрации.
 * Валидирует поля формы и вызывает AuthRepository.register().
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    /** Выполнить регистрацию */
    fun register() {
        val state = _uiState.value

        // Валидация
        val error = validateInput(state)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.register(state.email, state.name, state.password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /** Сброс флага успешной регистрации (после навигации) */
    fun onRegisterHandled() {
        _uiState.update { it.copy(isRegisterSuccess = false) }
    }

    /** Валидация полей формы */
    private fun validateInput(state: RegisterUiState): String? {
        if (state.email.isBlank()) return "Введите email"
        if (!state.email.contains("@")) return "Некорректный формат email"
        if (state.name.isBlank()) return "Введите имя пользователя"
        if (state.name.length < 2) return "Имя должно содержать минимум 2 символа"
        if (state.password.isBlank()) return "Введите пароль"
        if (state.password.length < 5) return "Пароль должен содержать минимум 5 символов"
        if (state.password != state.confirmPassword) return "Пароли не совпадают"
        return null
    }
}
