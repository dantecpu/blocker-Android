package io.github.newbugger.android.blocker

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import me.weishu.reflection.Reflection
import moe.shizuku.api.ShizukuClientHelper
import moe.shizuku.api.ShizukuService


class BlockerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogger()
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

    private fun initLogger() {
        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .t()
            .build()
        val androidPrinter = AndroidPrinter()
        val filePrinter = FilePrinter.Builder(filesDir.absolutePath)
            .backupStrategy(NeverBackupStrategy())
            .fileNameGenerator(ChangelessFileNameGenerator(LOG_FILENAME))
            .build()
        XLog.init(config, androidPrinter, filePrinter)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val LOG_FILENAME = "blocker_log.log"
    }

}
