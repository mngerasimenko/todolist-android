package ru.mngerasimenko.todolist.presentation.screen.todolist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mngerasimenko.todolist.domain.model.Todo
import ru.mngerasimenko.todolist.presentation.theme.TodoDoneColor
import ru.mngerasimenko.todolist.presentation.theme.TodoPendingColor

/**
 * Главный экран — список задач.
 * Поддерживает: создание задач (публичных/приватных), отметку выполнения,
 * свайп для удаления, pull-to-refresh, смену списка.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToListSelection: () -> Unit,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Навигация при выходе из системы
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.onLogoutHandled()
            onNavigateToLogin()
        }
    }

    // Навигация при смене списка
    LaunchedEffect(uiState.navigateToListSelection) {
        if (uiState.navigateToListSelection) {
            viewModel.onListSwitchHandled()
            onNavigateToListSelection()
        }
    }

    // Показ ошибки через Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Список задач")
                        if (uiState.listName.isNotBlank()) {
                            Text(
                                text = "${uiState.listName} \u00B7 ${uiState.userName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Кнопка смены списка
                    IconButton(onClick = { viewModel.switchList() }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Сменить список"
                        )
                    }
                    // Кнопка выхода
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Выйти"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поле для добавления новой задачи
            AddTodoBar(
                todoName = uiState.newTodoName,
                onNameChange = viewModel::onNewTodoNameChange,
                onAdd = viewModel::addTodo,
                isAdding = uiState.isAddingTodo,
                isPrivate = uiState.newTodoIsPrivate,
                onPrivateChange = viewModel::onNewTodoPrivateChange
            )

            // Фильтр задач
            FilterBar(
                filter = uiState.filter,
                todos = uiState.todos,
                onFilterChange = viewModel::setFilter
            )

            // Список задач с pull-to-refresh
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading && uiState.todos.isEmpty()) {
                    // Индикатор первоначальной загрузки
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredTodos.isEmpty()) {
                    // Пустой список (с учётом фильтра)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.todos.isEmpty()) "Нет задач. Добавьте первую!"
                            else "Нет задач в этой категории",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Список задач
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.filteredTodos,
                            key = { it.id }
                        ) { todo ->
                            TodoItem(
                                todo = todo,
                                onToggleDone = { viewModel.toggleTodoDone(todo) },
                                onDelete = { viewModel.deleteTodo(todo) }
                            )
                        }
                        // Отступ снизу
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

/** Панель добавления новой задачи */
@Composable
private fun AddTodoBar(
    todoName: String,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
    isAdding: Boolean,
    isPrivate: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = todoName,
            onValueChange = onNameChange,
            placeholder = { Text("Новая задача...") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            enabled = !isAdding
        )

        // Кнопка приватности
        IconButton(
            onClick = { onPrivateChange(!isPrivate) },
            enabled = !isAdding
        ) {
            Icon(
                imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isPrivate) "Приватная задача" else "Публичная задача",
                tint = if (isPrivate) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Кнопка добавления
        IconButton(
            onClick = onAdd,
            enabled = !isAdding && todoName.trim().length >= 2
        ) {
            if (isAdding) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить задачу",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Панель фильтрации задач: Все / Активные / Выполненные */
@Composable
private fun FilterBar(
    filter: TodoFilter,
    todos: List<Todo>,
    onFilterChange: (TodoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(TodoFilter.ALL, "Все", todos.size),
        Triple(TodoFilter.ACTIVE, "Активные", todos.count { !it.done }),
        Triple(TodoFilter.DONE, "Выполненные", todos.count { it.done }),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (value, label, count) ->
            FilterChip(
                selected = filter == value,
                onClick = { onFilterChange(value) },
                label = { Text("$label ($count)") }
            )
        }
    }
}

/** Парсинг HEX-цвета в Compose Color */
private fun parseHexColor(hex: String?): Color? {
    return try {
        hex?.let { Color(android.graphics.Color.parseColor(it)) }
    } catch (_: Exception) {
        null
    }
}

/** Элемент списка задач с поддержкой свайпа для удаления */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoItem(
    todo: Todo,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Красный фон при свайпе для удаления
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.error
                else Color.Transparent,
                label = "swipe_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Цветная полоска создателя
                val creatorColor = parseHexColor(todo.creatorColor)
                if (creatorColor != null) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(creatorColor)
                    )
                }

                // Иконка статуса (выполнена / нет)
                IconButton(onClick = onToggleDone) {
                    Icon(
                        imageVector = if (todo.done) Icons.Default.CheckCircle
                        else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (todo.done) "Выполнена" else "Не выполнена",
                        tint = if (todo.done) {
                            parseHexColor(todo.completorColor) ?: TodoDoneColor
                        } else TodoPendingColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Название и метаданные задачи
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (todo.done) TextDecoration.LineThrough
                            else TextDecoration.None
                        ),
                        color = if (todo.done) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Подпись: создатель и кто выполнил
                    val subtitle = buildString {
                        append(todo.userName ?: "")
                        if (todo.done && todo.completorUserName != null) {
                            append(" \u2022 \u2713 ${todo.completorUserName}")
                        }
                    }
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Иконка приватности
                if (todo.isPrivate) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Приватная",
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
