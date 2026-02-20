package ru.mngerasimenko.todolist.presentation.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.domain.model.Account
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.repository.AccountRepository
import ru.mngerasimenko.todolist.domain.repository.AuthRepository
import javax.inject.Inject

/** Состояние экрана списка аккаунтов */
data class AccountListUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Диалог создания аккаунта
    val showCreateDialog: Boolean = false,
    val createName: String = "",
    val createPassword: String = "",
    val isCreating: Boolean = false,
    // Диалог вступления в аккаунт
    val showJoinDialog: Boolean = false,
    val joinName: String = "",
    val joinPassword: String = "",
    val isJoining: Boolean = false,
    // Навигация
    val selectedAccountId: Long? = null,
    val isLoggedOut: Boolean = false
)

/**
 * ViewModel для экрана списка аккаунтов.
 * Загружает аккаунты пользователя, позволяет создавать и вступать в аккаунты.
 */
@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountListUiState())
    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    /** Загружает список аккаунтов пользователя */
    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = accountRepository.getMyAccounts()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(accounts = result.data, isLoading = false)
                    }
                }
                is Result.Error -> {
                    handleErrorResult(result)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /** Выбор аккаунта — сохраняет в TokenManager и переходит к задачам */
    fun selectAccount(account: Account) {
        viewModelScope.launch {
            tokenManager.saveAccount(account.id, account.name)
            _uiState.update { it.copy(selectedAccountId = account.id) }
        }
    }

    /** Сброс флага навигации после перехода */
    fun onAccountSelected() {
        _uiState.update { it.copy(selectedAccountId = null) }
    }

    // === Диалог создания аккаунта ===

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true, createName = "", createPassword = "") }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun onCreateNameChange(name: String) {
        _uiState.update { it.copy(createName = name) }
    }

    fun onCreatePasswordChange(password: String) {
        _uiState.update { it.copy(createPassword = password) }
    }

    fun createAccount() {
        val name = _uiState.value.createName.trim()
        val password = _uiState.value.createPassword
        if (name.length < 2 || password.length < 3) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }

            when (val result = accountRepository.createAccount(name, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isCreating = false, showCreateDialog = false) }
                    // Автоматически выбираем созданный аккаунт
                    selectAccount(result.data)
                }
                is Result.Error -> {
                    handleErrorResult(result)
                    _uiState.update {
                        it.copy(isCreating = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    // === Диалог вступления в аккаунт ===

    fun showJoinDialog() {
        _uiState.update { it.copy(showJoinDialog = true, joinName = "", joinPassword = "") }
    }

    fun dismissJoinDialog() {
        _uiState.update { it.copy(showJoinDialog = false) }
    }

    fun onJoinNameChange(name: String) {
        _uiState.update { it.copy(joinName = name) }
    }

    fun onJoinPasswordChange(password: String) {
        _uiState.update { it.copy(joinPassword = password) }
    }

    fun joinAccount() {
        val name = _uiState.value.joinName.trim()
        val password = _uiState.value.joinPassword
        if (name.length < 2 || password.length < 3) return

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true) }

            when (val result = accountRepository.joinAccount(name, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isJoining = false, showJoinDialog = false) }
                    // Автоматически выбираем аккаунт
                    selectAccount(result.data)
                }
                is Result.Error -> {
                    handleErrorResult(result)
                    _uiState.update {
                        it.copy(isJoining = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /** Выход из системы */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun onLogoutHandled() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** При 401 — перенаправляет на логин */
    private fun handleErrorResult(error: Result.Error) {
        if (error.code == 401) {
            viewModelScope.launch {
                authRepository.logout()
                _uiState.update { it.copy(isLoggedOut = true) }
            }
        }
    }
}
