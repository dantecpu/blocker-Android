package io.github.newbugger.android.blocker.core.shizuku.api

import android.content.ComponentName
import android.content.ComponentName.writeToParcel
import android.content.pm.PackageManager
import android.os.Parcel
import io.github.newbugger.android.blocker.core.shizuku.api.ShizukuSystemServer.getPackageManager
import io.github.newbugger.android.blocker.core.shizuku.api.ShizukuSystemServer.getParcelData
import moe.shizuku.api.ShizukuService
import java.lang.RuntimeException


object ShizukuApi {

    /**
     https://cs.android.com/android/platform/superproject/+/android10-release:frameworks
     /base/services/core/java/com/android/server/pm/PackageManagerService.java;l=21265-21284

     Shell can only change whole packages between ENABLED and DISABLED_USER states
     unless it is a test package.
     Shell cannot change component state.
    */

    /**
     https://github.com/lihenggui/blocker#shizuku-mode-no-root-permission-required

     Starting from Android O, if we install a Test-Only application,
     users could use pm command to control the command status.
     We could modify the install package to set it into Test-Only mode.

     me: a super dirty method, not recommended but supported for it here.
     */

    // though, really really not recommended usage
    fun setComponentRemote(comp: ComponentName, state: Int) {
        val data = getParcelData().also {
            writeToParcel(comp, it)
            it.writeInt(getState(state))
            it.writeInt(0)
            it.writeInt(0)
        }
        val reply = Parcel.obtain()
        try {
            ShizukuService.transactRemote(data, reply, 0)
            reply.readException()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw RuntimeException(e.message, e)
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

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
        } catch (e: Throwable) {
            e.printStackTrace()
            throw RuntimeException(e.message, e)
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    // though, really really not recommended usage
    fun setComponentWrapper(comp: ComponentName, state: Int) {
        try {
            getPackageManager().setComponentEnabledSetting(
                    comp, getState(state), 0, 0
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            throw RuntimeException(e.message, e)
        }
    }

    // method 1: use ShizukuBinderWrapper
    fun setApplicationWrapper(pack: String, state: Int) {
        try {
            getPackageManager().setApplicationEnabledSetting(
                    pack, getState(state), 0, 0, "android.content.pm.PackageManager"
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            throw RuntimeException(e.message, e)
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
