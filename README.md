# Todo List -- Android

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?logo=android)
![Hilt](https://img.shields.io/badge/Hilt-DI-FF6F00)
![Retrofit](https://img.shields.io/badge/Retrofit-2.x-48B983)

Nативное Android-приложение для управления списком задач. Подключается к [Todo List серверу](https://github.com/mngerasimenko/todo) через REST API с JWT-аутентификацией.

- **MVVM + Clean Architecture** -- четкое разделение слоев
- **Jetpack Compose** -- декларативный UI с Material3
- **Hilt** -- внедрение зависимостей
- **JWT** -- авторизация с автоматическим обновлением токена

---

## Демо

**Сервер:** [http://185.244.172.45:8090](http://185.244.172.45:8090)
Логин: `testUser` | Пароль: `testUser`

---

## Возможности

**Аккаунты**
- Создание и вступление в аккаунты (общее пространство для задач)
- Выбор активного аккаунта при запуске
- Переключение между аккаунтами без выхода из системы
- Роли: администратор (создатель) и участник

**Задачи**
- Просмотр задач аккаунта (pull-to-refresh)
- Добавление новых задач (публичных и приватных)
- Приватные задачи видны только создателю
- Отметка задач как выполненных (toggle)
- Удаление задач (swipe-to-delete)
- Цветные индикаторы создателя задачи
- Отображение имени пользователя, завершившего задачу

**Авторизация**
- Регистрация и вход по логину/паролю (BCrypt)
- Автоматическое обновление JWT-токена при истечении
- Автоматический выход при невалидном токене (401)
- Выход из аккаунта

**Интерфейс**
- Dark Mode и Material You (Android 12+)
- Отображение ошибок через Snackbar

**Версия:** 1.1.0

---

## Технологический стек

| Категория         | Технология                        | Версия  |
|-------------------|-----------------------------------|---------|
| **Язык**          | Kotlin                            | 2.0     |
| **UI**            | Jetpack Compose + Material3       | BOM     |
| **Архитектура**   | MVVM + Clean Architecture         | --      |
| **DI**            | Hilt (Dagger)                     | --      |
| **Сеть**          | Retrofit + OkHttp                 | 2.x     |
| **JSON**          | kotlinx.serialization             | --      |
| **Хранение**      | DataStore Preferences             | --      |
| **Навигация**     | Jetpack Navigation Compose        | --      |
| **Асинхронность** | Kotlin Coroutines + StateFlow     | --      |
| **Тестирование**  | JUnit + MockK + Turbine           | --      |
| **Min SDK**       | Android 8.0 (API 26)              | --      |
| **Target SDK**    | Android 15 (API 35)               | --      |
| **Сборка**        | Gradle + KSP                      | --      |

---

## Быстрый запуск

### Требования
- Android Studio Ladybug (2024.2+)
- JDK 17
- Android SDK 35

### Сборка и запуск

```bash
# Сборка debug APK
./gradlew assembleDebug

# Установка на подключенное устройство
adb install app/build/outputs/apk/debug/app-debug.apk

# Запуск тестов
./gradlew test
```

### Настройка URL сервера

По умолчанию приложение подключается к production-серверу `http://185.244.172.45:8090/`.

Для локальной разработки раскомментируйте строку в `app/build.gradle.kts`:
```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8090/\"")
}
```
> `10.0.2.2` -- это localhost хост-машины из Android-эмулятора.

---

## Архитектура

Приложение построено по принципу **Clean Architecture** с паттерном **MVVM**:

```
presentation/
  MainActivity -> NavGraph -> Screen <- ViewModel
  Экраны: Login, Register, AccountList, TodoList

        | вызывает
domain/
  Repository (интерфейсы)
  Model (Todo, User, Account)

        | реализует
data/
  RepositoryImpl -> ApiService (Retrofit HTTP)
                 -> TokenManager (DataStore)
                 -> DTO (JSON модели)
                 -> Interceptors (JWT Bearer)
```

### Навигация

```
Запуск приложения
  -> SplashViewModel проверяет авторизацию и наличие аккаунта
     -> Не авторизован -> LoginScreen -> RegisterScreen
     -> Авторизован, нет аккаунта -> AccountListScreen
     -> Авторизован, есть аккаунт -> TodoListScreen
```

### Поток данных (пример: создание задачи)

```
Пользователь вводит название задачи и нажимает "+"
  -> TodoListScreen (Compose UI)
    -> TodoListViewModel.addTodo()
      -> TodoRepository.createTodo(name, userId, accountId, isPrivate)
        -> TodoApiService POST /api/todos/create (Retrofit)
          -> Сервер возвращает TodoResponse
      -> UiState.todos = [новая задача] + текущие
    -> LazyColumn обновляется
```

---

## Структура проекта

```
app/src/main/java/ru/mngerasimenko/todolist/
├── domain/                          # Ядро (бизнес-логика)
│   ├── model/
│   │   ├── Account.kt                # Модель аккаунта и участника
│   │   ├── AuthResult.kt             # Result<T>: Success / Error
│   │   ├── Todo.kt                   # Модель задачи
│   │   └── User.kt                   # Модель пользователя
│   └── repository/
│       ├── AccountRepository.kt      # Интерфейс аккаунтов
│       ├── AuthRepository.kt         # Интерфейс авторизации
│       └── TodoRepository.kt         # Интерфейс задач
│
├── data/                            # Данные (сеть + хранение)
│   ├── local/
│   │   └── TokenManager.kt           # JWT токены + аккаунт в DataStore
│   ├── remote/
│   │   ├── ApiErrorParser.kt         # Парсинг ошибок сервера
│   │   ├── api/
│   │   │   ├── AccountApiService.kt  # Retrofit: аккаунты, задачи аккаунта
│   │   │   ├── AuthApiService.kt     # Retrofit: login, register, refresh
│   │   │   └── TodoApiService.kt     # Retrofit: create, update, delete
│   │   ├── dto/
│   │   │   ├── AccountDto.kt         # CreateAccountRequest, AccountResponse...
│   │   │   ├── AuthDto.kt            # LoginRequest, LoginResponse...
│   │   │   ├── ErrorDto.kt           # ErrorResponse
│   │   │   └── TodoDto.kt            # TodoRequest, TodoResponse
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt         # Bearer token в заголовки
│   │       └── TokenRefreshInterceptor.kt # Авто-обновление токена
│   └── repository/
│       ├── AccountRepositoryImpl.kt  # Реализация аккаунтов
│       ├── AuthRepositoryImpl.kt     # Реализация авторизации
│       └── TodoRepositoryImpl.kt     # Реализация задач
│
├── di/                              # Внедрение зависимостей (Hilt)
│   ├── AppModule.kt                  # Binds: интерфейс -> реализация
│   └── NetworkModule.kt              # OkHttp, Retrofit, API сервисы
│
└── presentation/                    # UI (Jetpack Compose)
    ├── TodoApp.kt                    # @HiltAndroidApp (точка входа)
    ├── MainActivity.kt               # Single Activity
    ├── navigation/
    │   ├── NavGraph.kt               # Маршруты: Auth -> Account -> TodoList
    │   └── SplashViewModel.kt        # Проверка авторизации и аккаунта
    ├── screen/
    │   ├── account/
    │   │   ├── AccountListScreen.kt  # UI списка аккаунтов
    │   │   └── AccountListViewModel.kt # Создание/вступление/выбор
    │   ├── login/
    │   │   ├── LoginScreen.kt        # UI экрана входа
    │   │   └── LoginViewModel.kt     # Логика входа
    │   ├── register/
    │   │   ├── RegisterScreen.kt     # UI регистрации
    │   │   └── RegisterViewModel.kt  # Логика регистрации
    │   └── todolist/
    │       ├── TodoListScreen.kt     # UI списка задач аккаунта
    │       └── TodoListViewModel.kt  # CRUD + приватность + цвета
    └── theme/
        ├── Color.kt                  # Палитра цветов
        ├── Theme.kt                  # Material3 + Dark Mode
        └── Type.kt                   # Типографика
```

---

## REST API

Приложение использует следующие эндпоинты сервера:

### Авторизация

| Метод | Эндпоинт            | Описание            |
|-------|----------------------|---------------------|
| POST  | /api/auth/login      | Вход                |
| POST  | /api/auth/register   | Регистрация         |
| POST  | /api/auth/refresh    | Обновление токена   |

### Аккаунты

| Метод  | Эндпоинт                    | Описание                    |
|--------|------------------------------|-----------------------------|
| GET    | /api/accounts                | Список аккаунтов            |
| POST   | /api/accounts                | Создать аккаунт             |
| POST   | /api/accounts/join           | Вступить в аккаунт          |
| GET    | /api/accounts/{id}/todos     | Задачи аккаунта             |
| GET    | /api/accounts/{id}/members   | Участники аккаунта          |
| DELETE | /api/accounts/{id}/leave     | Покинуть аккаунт            |

### Задачи

| Метод  | Эндпоинт            | Описание            |
|--------|----------------------|---------------------|
| POST   | /api/todos/create    | Создать задачу      |
| PUT    | /api/todos/{id}      | Обновить задачу     |
| DELETE | /api/todos/{id}      | Удалить задачу      |

---

## Связанные проекты

- [Todo List -- сервер](https://github.com/mngerasimenko/todo) -- Spring Boot бэкенд с REST API и Vaadin UI
