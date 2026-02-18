package ru.mngerasimenko.todolist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.mngerasimenko.todolist.presentation.navigation.NavGraph
import ru.mngerasimenko.todolist.presentation.theme.TodoListTheme

/**
 * Единственная Activity приложения (Single Activity Architecture).
 * Весь UI построен на Jetpack Compose с навигацией через NavGraph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoListTheme {
                NavGraph()
            }
        }
    }
}
