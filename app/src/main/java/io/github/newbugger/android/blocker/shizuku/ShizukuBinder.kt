package io.github.newbugger.android.blocker.shizuku

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import moe.shizuku.api.ShizukuApiConstants


object ShizukuBinder {

    private val TAG = javaClass.name

    private const val REQUEST_CODE_PERMISSION_V3 = 1

    fun shizukuRequestPermission(context: Context): Boolean {
        if (checkSelfPermission(context, ShizukuApiConstants.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(context as Activity, arrayOf(ShizukuApiConstants.PERMISSION), REQUEST_CODE_PERMISSION_V3)
            Log.d(TAG, "requesting shizuku permission")
        }
        return if (shouldShowRequestPermissionRationale(context as Activity, ShizukuApiConstants.PERMISSION)) {
            Log.d(TAG, "user denied the shizuku permission (shouldShowRequestPermissionRationale=true)")
            false
        } else {
            Log.d(TAG, "requested shizuku permission")
            true
        }
    }

}
