package io.github.newbugger.android.storage.directfileaccess

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


object DirectFileUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.getExternalDirectory(path: String? = null): String =
            if (Build.VERSION.SDK_INT >= 29) {
                this.getExternalFilesDirectory(path)
            } else {
                this.getExternalStorageDirectory(path)
            }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.getExternalFilesDirectory(path: String?): String =
            (this.getExternalFilesDir(null)!!.absolutePath).let {
                if (path == null) {
                    it
                } else {
                    it + File.separator + path
                }
            }.also {
                if (!File(it).exists()) File(it).mkdir()
            }

    @TargetApi(28)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.getExternalStorageDirectory(path: String?): String =
            this.let { ctx ->
                ctx.requestStorageReadPermission()
                ctx.requestStorageWritePermission()
                Environment.getExternalStorageDirectory().absolutePath
            }.let {
                if (path == null) {
                    it
                } else {
                    it + File.separator + path
                }
            }.also {
                if (!File(it).exists()) File(it).mkdir()
            }

    @TargetApi(28)
    private fun Context.requestStorageWritePermission(): Boolean {
        val requestCode = 1
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this as Activity, arrayOf(permission), requestCode)
        }
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun Context.requestStorageReadPermission(): Boolean {
        val requestCode = 1
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this as Activity, arrayOf(permission), requestCode)
        }
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

}
