package ru.mngerasimenko.todolist.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.mngerasimenko.todolist.data.repository.AccountRepositoryImpl
import ru.mngerasimenko.todolist.data.repository.AuthRepositoryImpl
import ru.mngerasimenko.todolist.data.repository.TodoRepositoryImpl
import ru.mngerasimenko.todolist.domain.repository.AccountRepository
import ru.mngerasimenko.todolist.domain.repository.AuthRepository
import ru.mngerasimenko.todolist.domain.repository.TodoRepository
import javax.inject.Singleton

/**
 * Hilt модуль для привязки интерфейсов к реализациям.
 * Связывает доменные интерфейсы с их конкретными реализациями из Data слоя.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
}
