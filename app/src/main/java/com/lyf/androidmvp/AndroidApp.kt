package com.lyf.androidmvp

import android.app.Application
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.LOG_DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 初始化 Toaster 吐司框架并设置优雅半透明圆角黑色微拟态样式
        Toaster.init(this)
        Toaster.setStyle(BlackToastStyle())
    }
}
