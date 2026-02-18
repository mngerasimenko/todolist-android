package ru.mngerasimenko.todolist.presentation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application класс приложения.
 * Аннотация @HiltAndroidApp запускает генерацию кода Hilt для DI.
 */
@HiltAndroidApp
class TodoApp : Application()
