package com.lyf.androidmvp.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class AppThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}

interface AppPreferences {
    val themeConfig: Flow<AppThemeConfig>
    val authToken: Flow<String?>
    val username: Flow<String?>

    /**
     * 内存级 Token 镜像读取（0ms 零阻塞），避免在 OkHttp 拦截器中产生 runBlocking 磁盘 IO
     */
    fun getCachedToken(): String?

    suspend fun setThemeConfig(theme: AppThemeConfig)
    suspend fun setAuthToken(token: String?)
    suspend fun setUsername(name: String?)
    suspend fun clear()
}

@Singleton
class AppPreferencesImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferences {

    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var isMirrorInitialized: Boolean = false

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USERNAME = stringPreferencesKey("username")
    }

    private val safePreferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    override val themeConfig: Flow<AppThemeConfig> = safePreferencesFlow.map { preferences ->
        when (preferences[PreferencesKeys.THEME_MODE]) {
            AppThemeConfig.LIGHT.name -> AppThemeConfig.LIGHT
            AppThemeConfig.DARK.name -> AppThemeConfig.DARK
            else -> AppThemeConfig.FOLLOW_SYSTEM
        }
    }

    override val authToken: Flow<String?> = safePreferencesFlow.map { preferences ->
        preferences[PreferencesKeys.AUTH_TOKEN]
    }

    override val username: Flow<String?> = safePreferencesFlow.map { preferences ->
        preferences[PreferencesKeys.USERNAME]
    }

    init {
        // 启动时在后台协程持续同步内存级 Token 镜像
        applicationScope.launch {
            authToken.collect { token ->
                cachedToken = token
                isMirrorInitialized = true
            }
        }
    }

    override fun getCachedToken(): String? {
        val current = cachedToken
        if (current != null || isMirrorInitialized) {
            return current
        }
        // 极早期冷启动竞态防御：若微秒级并发在 DataStore 磁盘首帧就绪前读取，执行一次极速带超时的首屏读取保底
        return try {
            runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(100L) {
                    authToken.first()
                }
            }.also {
                cachedToken = it
                isMirrorInitialized = true
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun setThemeConfig(theme: AppThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = theme.name
        }
    }

    override suspend fun setAuthToken(token: String?) {
        cachedToken = token
        dataStore.edit { preferences ->
            if (token == null) {
                preferences.remove(PreferencesKeys.AUTH_TOKEN)
            } else {
                preferences[PreferencesKeys.AUTH_TOKEN] = token
            }
        }
    }

    override suspend fun setUsername(name: String?) {
        dataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(PreferencesKeys.USERNAME)
            } else {
                preferences[PreferencesKeys.USERNAME] = name
            }
        }
    }

    override suspend fun clear() {
        cachedToken = null
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
