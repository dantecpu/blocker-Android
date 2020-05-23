package io.github.newbugger.android.blocker.core.shizuku

import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.ShizukuService
import moe.shizuku.api.SystemServiceHelper
import java.io.PrintWriter
import java.io.StringWriter


object ShizukuApi {

    /**
     https://cs.android.com/android/platform/superproject/+/android10-release:frameworks
     /base/services/core/java/com/android/server/pm/PackageManagerService.java;l=21265-21284

     Shell can only change whole packages between ENABLED and DISABLED_USER states
     unless it is a test package.
     Shell cannot change component state.
    */

    // method 2: use transactRemote directly
    // only available for APP disabler, cuz parcel cannot write ComponentName value
    // update: found in AIDL file
    fun setComponentRemote(pack: String, comp: String?, state: Int) {
        val data = SystemServiceHelper.obtainParcel("package",
                "android.content.pm.IPackageManager",
                "setApplicationEnabledSetting"
        )
        val reply = Parcel.obtain()
        data.also {
            /*if (comp != null)
                ComponentName(pack, comp).writeToParcel(it, 0)
            else
                it.writeString(pack)*/
            it.writeString(pack)
            it.writeInt(getState(state))
            it.writeInt(0)
            it.writeInt(0)
        }
        try {
            ShizukuService.transactRemote(data, reply, 0)
            reply.readException()
        } catch (e: RemoteException) {
            // TODO: how to put RemoteException into Throwable RuntimeException, for Oops window ?
            Log.e("io.github.newbugger.android.blocker.core.shizuku", "ShizukuApi", e)
            throw RemoteException(e.message)
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    // method 1: use ShizukuBinderWrapper
    // TODO: complete the abstract Class / interface code
    fun setComponentWrapper(pack: String, comp: String?, state: Int) {
        try {
            /*if (comp != null)
                getPackageManager().setComponentEnabledSetting(ComponentName(pack, comp), getState(state), 0, 0)
            else
                getPackageManager().setApplicationEnabledSetting(pack, getState(state), 0, 0)*/
            getPackageManager().setApplicationEnabledSetting(pack, getState(state), 0, 0)
        } catch (tr: Throwable) {
            throw RuntimeException(tr.message, tr)
        }
    }

    private fun getPackageManager(): IPackageManager {
        return IPackageManager.Stub.asInterface(
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
        )
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

    private fun getStackTrace(err: RemoteException): String {
        return StringWriter().let {
            err.printStackTrace(PrintWriter(it))
        }.toString()
    }

}
