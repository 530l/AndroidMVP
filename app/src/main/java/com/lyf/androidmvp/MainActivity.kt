package com.lyf.androidmvp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lyf.androidmvp.data.repository.SettingsRepository
import com.lyf.androidmvp.core.datastore.AppThemeConfig
import com.lyf.androidmvp.navigation.AppNavHost
import com.lyf.androidmvp.ui.theme.AndroidMVPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 关键防御：防止从桌面启动器点击图标唤起已有任务时，系统重复创建根 Activity 实例导致状态被重置覆盖
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            val themeConfig by settingsRepository.themeConfig.collectAsStateWithLifecycle(
                initialValue = AppThemeConfig.FOLLOW_SYSTEM
            )
            val isDarkTheme = when (themeConfig) {
                AppThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                AppThemeConfig.LIGHT -> false
                AppThemeConfig.DARK -> true
            }

            AndroidMVPTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}