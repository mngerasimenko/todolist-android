package ru.mngerasimenko.todolist.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер JWT токенов.
 * Хранит access token, refresh token и данные пользователя
 * в Android DataStore (зашифрованное хранилище).
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val LIST_ID_KEY = longPreferencesKey("list_id")
        private val LIST_NAME_KEY = stringPreferencesKey("list_name")
    }

    /** Flow с access токеном (null если не авторизован) */
    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN_KEY]
    }

    /** Flow с refresh токеном */
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN_KEY]
    }

    /** Flow с ID пользователя */
    val userIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    /** Flow с именем пользователя */
    val userNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY]
    }

    /** Flow с ID текущего списка */
    val listIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[LIST_ID_KEY]
    }

    /** Flow с названием текущего списка */
    val listNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LIST_NAME_KEY]
    }

    /** Flow — авторизован ли пользователь */
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[ACCESS_TOKEN_KEY].isNullOrBlank()
    }

    /** Сохраняет JWT токены после входа/регистрации */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    /** Сохраняет данные пользователя */
    suspend fun saveUser(userId: Long, userName: String, userEmail: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = userName
            prefs[USER_EMAIL_KEY] = userEmail
        }
    }

    /** Сохраняет выбранный список */
    suspend fun saveList(listId: Long, listName: String) {
        context.dataStore.edit { prefs ->
            prefs[LIST_ID_KEY] = listId
            prefs[LIST_NAME_KEY] = listName
        }
    }

    /** Очищает выбранный список (при смене списка) */
    suspend fun clearList() {
        context.dataStore.edit { prefs ->
            prefs.remove(LIST_ID_KEY)
            prefs.remove(LIST_NAME_KEY)
        }
    }

    /** Очищает все данные (при выходе из системы) */
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
