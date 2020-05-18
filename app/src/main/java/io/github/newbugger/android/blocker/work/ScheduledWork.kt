package io.github.newbugger.android.blocker.work

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elvishew.xlog.XLog
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.DeviceUtil
import io.github.newbugger.android.libkit.utils.ManagerUtils
import io.github.newbugger.android.libkit.utils.PermissionUtils
import java.util.*

class ScheduledWork(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    private val logger = XLog.tag("ScheduledWork").build()
    override fun doWork(): Result {
        logger.d("Start to execute: time ${Date()}")
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        val isAutoBlockOn = preferenceManager.getBoolean(context.getString(R.string.key_pref_auto_block), false)
        val isForceDozeOn = preferenceManager.getBoolean(context.getString(R.string.key_pref_force_doze), false)
        val isScreenOff = !DeviceUtil.isScreenOn(context)
        val isRooted = PermissionUtils.isRootAvailable
        if (!isRooted) {
            logger.d("Can't get root permission, exiting...")
            return Result.failure()
        }
        try {
            if (isScreenOff) {
                if (isAutoBlockOn) {
                    ApplicationUtil.getBlockedApplication(context).forEach {
                        ManagerUtils.forceStop(it)
                    }
                }
                if (isForceDozeOn) {
                    DeviceUtil.forceDoze()
                }
            }
        } catch (e: Exception) {
            logger.e(e)
            return Result.retry()
        }
        return Result.success()
    }
}