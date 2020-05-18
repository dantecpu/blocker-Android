package io.github.newbugger.android.blocker.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.newbugger.android.blocker.R


class NotificationUtil {
    val vProcessingIndicatorChannelId = "processing_progress_indicator"
    private val vProcessingNotificationId = 1
    private lateinit var builder: NotificationCompat.Builder
    fun createProcessingNotification(context: Context, total: Int) {
        builder = NotificationCompat.Builder(context, vProcessingIndicatorChannelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.processing))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(total, 0, true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(vProcessingNotificationId, builder.build())
    }

    fun finishProcessingNotification(context: Context, count: Int) {
        // {Count} components were set, {count} succeeded,{count} failed
        builder.setContentTitle(context.getString(R.string.done))
                .setContentText(context.getString(R.string.notification_done, count))
                .setSubText("Blocker")
                .setProgress(0, 0, false)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(vProcessingNotificationId, builder.build())
    }

    fun updateProcessingNotification(context: Context, appLabel: String, current: Int, total: Int) {
        builder.setProgress(total, current, false)
                .setContentText(context.getString(R.string.processing_indicator, current, total))
                .setSubText(appLabel)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(vProcessingNotificationId, builder.build())
    }

    fun cancelNotification(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(vProcessingNotificationId)
    }
}
