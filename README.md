# Todo List — Android

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?logo=android)
![Hilt](https://img.shields.io/badge/Hilt-DI-FF6F00)
![Retrofit](https://img.shields.io/badge/Retrofit-2.x-48B983)

Нативное Android-приложение для управления списком задач. Подключается к [Todo List серверу](https://github.com/mngerasimenko/todo) через REST API с JWT-аутентификацией.

- **MVVM + Clean Architecture** — чёткое разделение слоёв
- **Jetpack Compose** — декларативный UI с Material3
- **Hilt** — внедрение зависимостей
- **JWT** — авторизация с автоматическим обновлением токена

---

## Демо

**Сервер:** [http://185.244.172.45:8090](http://185.244.172.45:8090)
Логин: `testUser` | Пароль: `testUser`

---

## Возможности

- Авторизация и регистрация пользователей
- Просмотр списка задач (pull-to-refresh)
- Добавление новых задач
- Отметка задач как выполненных (toggle)
- Удаление задач (swipe-to-delete)
- Выход из аккаунта
- Поддержка Dark Mode и Material You (Android 12+)
- Автоматическое обновление JWT-токена при истечении
- Автоматический выход при невалидном токене (401)
- Отображение ошибок при операциях с задачами

**Статус:** Активная разработка
**Версия:** 1.0.0

---

## Технологический стек

| Категория         | Технология                        | Версия  |
|-------------------|-----------------------------------|---------|
| **Язык**          | Kotlin                            | 2.0     |
| **UI**            | Jetpack Compose + Material3       | BOM     |
| **Архитектура**   | MVVM + Clean Architecture         | —       |
| **DI**            | Hilt (Dagger)                     | —       |
| **Сеть**          | Retrofit + OkHttp                 | 2.x     |
| **JSON**          | kotlinx.serialization             | —       |
| **Хранение**      | DataStore Preferences             | —       |
| **Навигация**     | Jetpack Navigation Compose        | —       |
| **Асинхронность** | Kotlin Coroutines + StateFlow     | —       |
| **Тестирование**  | JUnit + MockK + Turbine           | —       |
| **Min SDK**       | Android 8.0 (API 26)              | —       |
| **Target SDK**    | Android 15 (API 35)               | —       |
| **Сборка**        | Gradle + KSP                      | —       |

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

# Установка на подключённое устройство
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
> `10.0.2.2` — это localhost хост-машины из Android-эмулятора.

---

## Архитектура

Приложение построено по принципу **Clean Architecture** с паттерном **MVVM**:

```
┌─────────────────────────────────────────────────────┐
│                  presentation/                       │
│                                                     │
│  MainActivity ─→ NavGraph ─→ Screen ←─ ViewModel   │
│                                           │         │
│  Screen: Compose UI (LoginScreen,         │         │
│          RegisterScreen, TodoListScreen)   │         │
└───────────────────────────────────────────┼─────────┘
                                            │ вызывает
┌───────────────────────────────────────────┼─────────┐
│                   domain/                 │         │
│                                           ▼         │
│  Repository (интерфейс) ←── Model (Todo, User)     │
└───────────────────────────────────────────┬─────────┘
                                            │ реализует
┌───────────────────────────────────────────┼─────────┐
│                    data/                  │         │
│                                           ▼         │
│  RepositoryImpl ─→ ApiService (Retrofit HTTP)       │
│                 ─→ TokenManager (DataStore)          │
│                 ─→ DTO (JSON модели)                │
│                 ─→ Interceptors (JWT Bearer)         │
└─────────────────────────────────────────────────────┘
```

### Поток данных (пример: вход)

```
Пользователь нажимает "Войти"
  → LoginScreen (Compose UI)
    → LoginViewModel.login()
      → AuthRepository.login(username, password)
        → AuthApiService POST /api/auth/login (Retrofit)
          → Сервер возвращает JWT токены
        → TokenManager сохраняет токены в DataStore
      → UiState(isLoginSuccess = true)
    → NavGraph → переход на TodoListScreen
      → TodoApiService GET /api/todos/user/{id} (с Bearer token)
    → Список задач на экране
```

---

## Структура проекта

```
app/src/main/java/ru/mngerasimenko/todolist/
├── domain/                          # Ядро (бизнес-логика)
│   ├── model/
│   │   ├── AuthResult.kt              # Result<T>: Success / Error
│   │   ├── Todo.kt                    # Модель задачи
│   │   └── User.kt                    # Модель пользователя
│   └── repository/
│       ├── AuthRepository.kt          # Интерфейс авторизации
│       └── TodoRepository.kt          # Интерфейс задач
│
├── data/                            # Данные (сеть + хранение)
│   ├── local/
│   │   └── TokenManager.kt            # JWT токены в DataStore
│   ├── remote/
│   │   ├── ApiErrorParser.kt          # Парсинг ошибок сервера
│   │   ├── api/
│   │   │   ├── AuthApiService.kt      # Retrofit: login, register, refresh
│   │   │   └── TodoApiService.kt      # Retrofit: CRUD задач
│   │   ├── dto/
│   │   │   ├── AuthDto.kt             # LoginRequest, LoginResponse...
│   │   │   ├── ErrorDto.kt            # ErrorResponse
│   │   │   └── TodoDto.kt             # TodoRequest, TodoResponse
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt         # Bearer token в заголовки
│   │       └── TokenRefreshInterceptor.kt # Авто-обновление токена
│   └── repository/
│       ├── AuthRepositoryImpl.kt      # Реализация авторизации
│       └── TodoRepositoryImpl.kt      # Реализация задач
│
├── di/                              # Внедрение зависимостей (Hilt)
│   ├── AppModule.kt                   # Binds: интерфейс → реализация
│   └── NetworkModule.kt               # OkHttp, Retrofit, API сервисы
│
└── presentation/                    # UI (Jetpack Compose)
    ├── TodoApp.kt                     # @HiltAndroidApp (точка входа)
    ├── MainActivity.kt                # Single Activity
    ├── navigation/
    │   ├── NavGraph.kt                # Маршруты между экранами
    │   └── SplashViewModel.kt        # Проверка авторизации
    ├── screen/
    │   ├── login/
    │   │   ├── LoginScreen.kt         # UI экрана входа
    │   │   └── LoginViewModel.kt      # Логика входа
    │   ├── register/
    │   │   ├── RegisterScreen.kt      # UI регистрации
    │   │   └── RegisterViewModel.kt   # Логика регистрации
    │   └── todolist/
    │       ├── TodoListScreen.kt      # UI списка задач
    │       └── TodoListViewModel.kt   # CRUD + swipe-to-delete
    └── theme/
        ├── Color.kt                   # Палитра цветов
        ├── Theme.kt                   # Material3 + Dark Mode
        └── Type.kt                    # Типографика
```

---

## Связанные проекты

- [Todo List — сервер](https://github.com/mngerasimenko/todo) — Spring Boot бэкенд с REST API и Vaadin UI
