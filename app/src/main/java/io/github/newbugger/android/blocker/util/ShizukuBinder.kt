package io.github.newbugger.android.blocker.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import moe.shizuku.api.RemoteProcess
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.api.ShizukuClientHelper
import moe.shizuku.api.ShizukuService


// https://github.com/RikkaApps/Shizuku/blob/master/sample/src/main/java/moe/shizuku/
// sample/MainActivity.java

object ShizukuBinder {

    private const val TAG = "io.github.newbugger.android.blocker.util.ShizukuBinder"

    private const val REQUEST_CODE_PERMISSION_V3 = 1

    fun shizukuTestV3() {
        try {
            val remoteProcess: RemoteProcess = ShizukuService.newProcess(arrayOf("sh"), null, null)
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
                Log.d(TAG, "fun shizukuTestV3: newProcess: $remoteProcess")
                Log.d(TAG, "fun shizukuTestV3: waitFor: " + remoteProcess.waitFor())
                Log.d(TAG, "fun shizukuTestV3: output: $string")
            }
        } catch (tr: Throwable) {
            Log.e(TAG, "fun shizukuTestV3: err: ", tr)
        }
    }

    fun shizukuUnSetBroadcast(context: Context, shizukuBinderReceiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(shizukuBinderReceiver)
    }

    fun shizukuSetBroadcast(context: Context, shizukuBinderReceiver: BroadcastReceiver) {
        val action = "moe.shizuku.client.intent.action.SEND_BINDER"
        LocalBroadcastManager.getInstance(context).registerReceiver(shizukuBinderReceiver, IntentFilter(action))
    }

    fun shizukuRequestPermission(context: Context): Boolean {
        return if (ActivityCompat.checkSelfPermission(context,
                        ShizukuApiConstants.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity,
                    arrayOf(ShizukuApiConstants.PERMISSION), REQUEST_CODE_PERMISSION_V3)
            Log.d(TAG, "requesting shizuku permission ..")
            false
        } else {
            true
        }
    }

    fun shizukuIsInstalled(context: Context): Boolean {
        ShizukuClientHelper.isManagerV3Installed(context).also {
            if (!it) {
                Log.d(TAG, "Shizuku Manager is not installed or too low version")
                Toast.makeText(context, "Shizuku Manager not installed or too low.", Toast.LENGTH_LONG).show()
            }
            return it
        }
    }

}
