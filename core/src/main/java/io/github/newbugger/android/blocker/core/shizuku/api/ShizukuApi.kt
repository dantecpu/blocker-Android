package io.github.newbugger.android.blocker.core.shizuku.api

import android.content.pm.PackageManager
import android.os.Parcel
import android.os.RemoteException
import io.github.newbugger.android.blocker.core.shizuku.api.ShizukuSystemServer.getPackageManager
import io.github.newbugger.android.blocker.core.shizuku.api.ShizukuSystemServer.getParcelData
import moe.shizuku.api.ShizukuService


object ShizukuApi {

    /**
     https://cs.android.com/android/platform/superproject/+/android10-release:frameworks
     /base/services/core/java/com/android/server/pm/PackageManagerService.java;l=21265-21284

     Shell can only change whole packages between ENABLED and DISABLED_USER states
     unless it is a test package.
     Shell cannot change component state.
    */

    // method 2: use transactRemote directly
    fun setApplicationRemote(pack: String, state: Int) {
        val data = getParcelData().apply {
            writeString(pack)
            writeInt(getState(state))
            writeInt(0)
            writeInt(0)
        }
        val reply = Parcel.obtain()
        try {
            ShizukuService.transactRemote(data, reply, 0)
            reply.readException()
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    // method 1: use ShizukuBinderWrapper
    // TODO: complete the abstract Class / interface code
    fun setApplicationWrapper(pack: String, state: Int) {
        try {
            getPackageManager().setApplicationEnabledSetting(
                    pack, getState(state), 0, 0
            )
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun getState(state: Int): Int {
        return when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED ->
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED ->
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            else ->
                state
        }
    }

}
