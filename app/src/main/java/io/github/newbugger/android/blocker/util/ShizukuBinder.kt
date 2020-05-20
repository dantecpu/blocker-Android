package io.github.newbugger.android.blocker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elvishew.xlog.Logger
import com.elvishew.xlog.XLog
import moe.shizuku.api.ShizukuClientHelper
import moe.shizuku.api.ShizukuService


class ShizukuBinder {

    private val logger: Logger = XLog.tag("ShizukuBinder").build()

    private val shizukuBinderReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logger.d("shizukuBinderReceiver: " + ShizukuService.getBinder())
        }
    }

    // https://github.com/RikkaApps/Shizuku/blob/master/sample/src/main/java/moe/shizuku/
    // sample/MainActivity.java
    private fun shizukuTestV3() {
        while (ShizukuService.pingBinder()) break
        try {
            val remoteProcess = ShizukuService.newProcess(arrayOf("sh"), null, null)
            remoteProcess.outputStream.apply {
                write("echo test\n".toByteArray())
                write("id\n".toByteArray())
                write("exit\n".toByteArray())
                close()
            }
            remoteProcess.inputStream.also { input ->
                val string = StringBuilder()
                var c: Int
                while (input.read().also { c = it } != -1) {
                    string.append(c.toChar())
                }
                input.close()
                logger.d("newProcess: $remoteProcess")
                logger.d("waitFor: " + remoteProcess.waitFor())
                logger.d("output: $string")
            }
        } catch (tr: Throwable) {
            logger.e("newProcess", tr)
        }
    }

    private fun shizukuBroadcast(context: Context) {
        val action = "moe.shizuku.client.intent.action.SEND_BINDER"
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(
                        shizukuBinderReceiver,
                        IntentFilter(action)
                )
    }

    fun shizukuCreate(context: Context) {
        shizukuLogTest()
        if (!ShizukuClientHelper.isManagerV2Installed(context)) {
            logger.e("Shizuku is not installed or too low version")
            return
        }
        shizukuBroadcast(context)
        shizukuTestV3()
    }

    fun shizukuDestory(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(shizukuBinderReceiver)
        shizukuLogTest()
    }

    private fun shizukuLogTest() {
        logger.d("this is a shizukuBinder test.")
    }

}
