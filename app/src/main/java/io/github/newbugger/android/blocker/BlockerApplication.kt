package io.github.newbugger.android.blocker

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.Shell
import io.github.newbugger.android.blocker.core.libsu.LibsuInitializer
import me.weishu.reflection.Reflection
import moe.shizuku.api.ShizukuClientHelper
import moe.shizuku.api.ShizukuService


class BlockerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
        createNotificationChannel()
    }

    // Inside ShizukuBinderReceiveProvider, things are very simple
    // the received binder is stored to a static field in ShizukuService
    // and call the OnBinderReceivedListener (again, a static field is used to store the listener)
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
        ShizukuClientHelper.setBinderReceivedListener {
            Log.d("blocker.Application", "attachBaseContext: onBinderReceived")
            while (ShizukuService.getBinder() == null || !ShizukuService.pingBinder()) {
                Log.d("blocker.Application", "ShizukuService: binder is null, keep waiting ..")
            }
            try {
                val action = "io.github.newbugger.android.blocker.intent.BROADCAST"
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
                Log.d("blocker.Application", "sendBroadcast: broadcast sent")
            } catch (tr: Throwable) {
                Log.e("blocker.Application", "catch: can't contact with remote", tr)
                return@setBinderReceivedListener
            }
        }
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
