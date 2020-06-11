package io.github.newbugger.android.blocker.shizuku

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import moe.shizuku.api.ShizukuApiConstants


object ShizukuBinder {

    private const val TAG = "io.github.newbugger.android.blocker.shizuku.ShizukuBinder"

    private const val REQUEST_CODE_PERMISSION_V3 = 1

    fun shizukuRequestPermission(context: Context): Boolean {
        return if (checkSelfPermission(context, ShizukuApiConstants.PERMISSION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(context as Activity,
                    arrayOf(ShizukuApiConstants.PERMISSION), REQUEST_CODE_PERMISSION_V3)
            Log.d(TAG, "requesting shizuku permission ..")
            false
        } else {
            Log.d(TAG, "shizuku permission requested.")
            true
        }
    }

}
