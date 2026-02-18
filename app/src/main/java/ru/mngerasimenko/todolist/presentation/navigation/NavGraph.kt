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
import ru.mngerasimenko.todolist.presentation.screen.login.LoginScreen
import ru.mngerasimenko.todolist.presentation.screen.register.RegisterScreen
import ru.mngerasimenko.todolist.presentation.screen.todolist.TodoListScreen

// Type-safe маршруты навигации
@Serializable object AuthGraph
@Serializable object Login
@Serializable object Register
@Serializable object TodoList

/**
 * Навигационный граф приложения.
 * Определяет маршруты: Login → TodoList, Register → TodoList.
 * Проверяет состояние авторизации при запуске через SplashViewModel.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsStateWithLifecycle()

    // Ждём определения состояния авторизации
    if (isLoggedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) TodoList else AuthGraph
    ) {
        // Вложенный граф аутентификации (Login + Register)
        navigation<AuthGraph>(startDestination = Login) {
            composable<Login> {
                LoginScreen(
                    onNavigateToTodoList = {
                        navController.navigate(TodoList) {
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
                    onNavigateToTodoList = {
                        navController.navigate(TodoList) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Экран списка задач (главный)
        composable<TodoList> {
            TodoListScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthGraph) {
                        popUpTo<TodoList> { inclusive = true }
                    }
                }
            )
        }
    }
}
