package ru.mngerasimenko.todolist.presentation.screen.listselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.domain.model.TaskList
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.repository.ListRepository
import ru.mngerasimenko.todolist.domain.repository.AuthRepository
import javax.inject.Inject

/** Состояние экрана выбора списка */
data class ListSelectionUiState(
    val lists: List<TaskList> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Диалог создания списка
    val showCreateDialog: Boolean = false,
    val createName: String = "",
    val createPassword: String = "",
    val isCreating: Boolean = false,
    // Диалог вступления в список
    val showJoinDialog: Boolean = false,
    val joinName: String = "",
    val joinPassword: String = "",
    val isJoining: Boolean = false,
    // Навигация
    val selectedListId: Long? = null,
    val isLoggedOut: Boolean = false
)

/**
 * ViewModel для экрана выбора списка.
 * Загружает списки пользователя, позволяет создавать и вступать в списки.
 */
@HiltViewModel
class ListSelectionViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListSelectionUiState())
    val uiState: StateFlow<ListSelectionUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    /** Загружает списки пользователя */
    fun loadLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = listRepository.getMyLists()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(lists = result.data, isLoading = false)
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

    /** Выбор списка — сохраняет в TokenManager и переходит к задачам */
    fun selectList(taskList: TaskList) {
        viewModelScope.launch {
            tokenManager.saveList(taskList.id, taskList.name)
            _uiState.update { it.copy(selectedListId = taskList.id) }
        }
    }

    /** Сброс флага навигации после перехода */
    fun onListSelected() {
        _uiState.update { it.copy(selectedListId = null) }
    }

    // === Диалог создания списка ===

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

    fun createList() {
        val name = _uiState.value.createName.trim()
        val password = _uiState.value.createPassword
        if (name.length < 2 || password.length < 3) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }

            when (val result = listRepository.createList(name, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isCreating = false, showCreateDialog = false) }
                    // Автоматически выбираем созданный список
                    selectList(result.data)
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

    // === Диалог вступления в список ===

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

    fun joinList() {
        val name = _uiState.value.joinName.trim()
        val password = _uiState.value.joinPassword
        if (name.length < 2 || password.length < 3) return

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true) }

            when (val result = listRepository.joinList(name, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isJoining = false, showJoinDialog = false) }
                    // Автоматически выбираем список
                    selectList(result.data)
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
