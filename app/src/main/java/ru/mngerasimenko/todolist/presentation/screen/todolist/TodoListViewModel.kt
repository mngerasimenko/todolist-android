package ru.mngerasimenko.todolist.presentation.screen.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mngerasimenko.todolist.data.local.TokenManager
import ru.mngerasimenko.todolist.domain.model.Result
import ru.mngerasimenko.todolist.domain.model.Todo
import ru.mngerasimenko.todolist.domain.repository.AccountRepository
import ru.mngerasimenko.todolist.domain.repository.AuthRepository
import ru.mngerasimenko.todolist.domain.repository.TodoRepository
import javax.inject.Inject

/** Состояние экрана списка задач */
data class TodoListUiState(
    val todos: List<Todo> = emptyList(),
    val userName: String = "",
    val accountName: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val newTodoName: String = "",
    val newTodoIsPrivate: Boolean = false,
    val isAddingTodo: Boolean = false,
    val isLoggedOut: Boolean = false,
    val navigateToAccountList: Boolean = false
)

/**
 * ViewModel для экрана списка задач.
 * Загружает задачи аккаунта, позволяет создавать, обновлять,
 * удалять задачи и выходить из системы.
 */
@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private var currentUserId: Long = -1
    private var currentAccountId: Long = -1

    init {
        loadUserAndTodos()
    }

    /** Загружает данные пользователя и аккаунта из TokenManager, затем задачи */
    private fun loadUserAndTodos() {
        viewModelScope.launch {
            val userId = tokenManager.userIdFlow.first()
            val userName = tokenManager.userNameFlow.first()
            val accountId = tokenManager.accountIdFlow.first()
            val accountName = tokenManager.accountNameFlow.first()

            if (userId == null || accountId == null) {
                _uiState.update { it.copy(isLoggedOut = true) }
                return@launch
            }

            currentUserId = userId
            currentAccountId = accountId
            _uiState.update {
                it.copy(
                    userName = userName ?: "",
                    accountName = accountName ?: ""
                )
            }
            loadTodos()
        }
    }

    /** Загружает задачи аккаунта с сервера */
    fun loadTodos() {
        if (currentAccountId == -1L) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = accountRepository.getTodosByAccount(currentAccountId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            todos = result.data.sortedByDescending { todo -> todo.id },
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is Result.Error -> {
                    handleErrorResult(result)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /** Обновление списка (pull-to-refresh) */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadTodos()
    }

    /** Обновление текста новой задачи */
    fun onNewTodoNameChange(name: String) {
        _uiState.update { it.copy(newTodoName = name) }
    }

    /** Переключение приватности новой задачи */
    fun onNewTodoPrivateChange(isPrivate: Boolean) {
        _uiState.update { it.copy(newTodoIsPrivate = isPrivate) }
    }

    /** Создание новой задачи */
    fun addTodo() {
        val name = _uiState.value.newTodoName.trim()
        val isPrivate = _uiState.value.newTodoIsPrivate
        if (name.isBlank() || name.length < 2) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingTodo = true) }

            when (val result = todoRepository.createTodo(name, currentUserId, currentAccountId, isPrivate)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            todos = listOf(result.data) + it.todos,
                            newTodoName = "",
                            newTodoIsPrivate = false,
                            isAddingTodo = false
                        )
                    }
                }
                is Result.Error -> {
                    handleErrorResult(result)
                    _uiState.update {
                        it.copy(isAddingTodo = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    /** Переключение статуса задачи (выполнена / не выполнена) */
    fun toggleTodoDone(todo: Todo) {
        viewModelScope.launch {
            // Оптимистичное обновление UI
            _uiState.update { state ->
                state.copy(
                    todos = state.todos.map {
                        if (it.id == todo.id) it.copy(done = !it.done) else it
                    }
                )
            }

            val result = todoRepository.updateTodo(
                todo.id, todo.name, !todo.done, todo.userId, currentAccountId
            )
            if (result is Result.Error) {
                handleErrorResult(result)
                // Откатываем при ошибке и показываем сообщение
                _uiState.update { state ->
                    state.copy(
                        todos = state.todos.map {
                            if (it.id == todo.id) it.copy(done = todo.done) else it
                        },
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /** Удаление задачи */
    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            // Оптимистичное удаление из UI
            val previousTodos = _uiState.value.todos
            _uiState.update { it.copy(todos = it.todos.filter { t -> t.id != todo.id }) }

            val result = todoRepository.deleteTodo(todo.id)
            if (result is Result.Error) {
                handleErrorResult(result)
                // Откатываем при ошибке и показываем сообщение
                _uiState.update { it.copy(todos = previousTodos, errorMessage = result.message) }
            }
        }
    }

    /** Смена аккаунта — переход к списку аккаунтов */
    fun switchAccount() {
        viewModelScope.launch {
            tokenManager.clearAccount()
            _uiState.update { it.copy(navigateToAccountList = true) }
        }
    }

    fun onAccountSwitchHandled() {
        _uiState.update { it.copy(navigateToAccountList = false) }
    }

    /** Выход из системы */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    /** Сброс флага выхода (после навигации) */
    fun onLogoutHandled() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }

    /** Сброс ошибки */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Проверяет, является ли ошибка 401 (токен истёк) — перенаправляет на логин */
    private fun handleErrorResult(error: Result.Error) {
        if (error.code == 401) {
            viewModelScope.launch {
                authRepository.logout()
                _uiState.update { it.copy(isLoggedOut = true) }
            }
        }
    }
}
