package ru.mngerasimenko.todolist.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.mngerasimenko.todolist.presentation.screen.listselection.ListSelectionScreen
import ru.mngerasimenko.todolist.presentation.screen.login.LoginScreen
import ru.mngerasimenko.todolist.presentation.screen.register.RegisterScreen
import ru.mngerasimenko.todolist.presentation.screen.todolist.TodoListScreen

// Type-safe маршруты навигации
@Serializable object AuthGraph
@Serializable object Login
@Serializable object Register
@Serializable object ListSelection
@Serializable object TodoList

/**
 * Навигационный граф приложения.
 * Маршруты: Login/Register → ListSelection → TodoList.
 * Проверяет состояние авторизации и наличие списка при запуске.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val hasList by splashViewModel.hasList.collectAsStateWithLifecycle()

    // Ждём определения состояния авторизации
    if (isLoggedIn == null || hasList == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Определяем стартовый экран
    val startDestination: Any = when {
        isLoggedIn != true -> AuthGraph
        hasList == true -> TodoList
        else -> ListSelection
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Вложенный граф аутентификации (Login + Register)
        navigation<AuthGraph>(startDestination = Login) {
            composable<Login> {
                LoginScreen(
                    onNavigateToListSelection = {
                        navController.navigate(ListSelection) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Register)
                    }
                )
            }

            composable<Register> {
                RegisterScreen(
                    onNavigateToListSelection = {
                        navController.navigate(ListSelection) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Экран выбора списка
        composable<ListSelection> {
            ListSelectionScreen(
                onNavigateToTodoList = {
                    navController.navigate(TodoList) {
                        popUpTo<ListSelection> { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthGraph) {
                        popUpTo<ListSelection> { inclusive = true }
                    }
                }
            )
        }

        // Экран списка задач (главный)
        composable<TodoList> {
            TodoListScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthGraph) {
                        popUpTo<TodoList> { inclusive = true }
                    }
                },
                onNavigateToListSelection = {
                    navController.navigate(ListSelection) {
                        popUpTo<TodoList> { inclusive = true }
                    }
                }
            )
        }
    }
}
