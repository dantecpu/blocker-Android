package io.github.newbugger.android.blocker

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.Shell
import io.github.newbugger.android.blocker.libsu.LibsuInitializer
import me.weishu.reflection.Reflection


class BlockerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun attachBaseContext(base: Context) {
        context = base
        Reflection.unseal(base) // the dependency of shizuku as to keep there
        super.attachBaseContext(base)
    }

    private fun createNotificationChannel() {
        val channelId = "processing_progress_indicator"
        val channelName = context.getString(R.string.processing_progress_indicator)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // from: topjohnwu/Magisk/blob/master/app/src/main/java/com/topjohnwu/magisk/core/App.kt
    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setTimeout(10)
        Shell.Config.addInitializers(LibsuInitializer::class.java)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val LOG_FILENAME = "blocker_log.log"
    }

}
