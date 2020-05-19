package io.github.newbugger.android.blocker

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import moe.shizuku.api.ShizukuClient


class BlockerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogger()
        context = this
        createNotificationChannel()
        ShizukuClient.initialize(this)
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
