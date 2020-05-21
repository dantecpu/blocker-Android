package io.github.newbugger.android.blocker.core.shizuku

import android.content.ComponentName
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.os.Parcel
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.ShizukuService
import moe.shizuku.api.SystemServiceHelper


object ShizukuApi {

    // method 2: use transactRemote directly
    // only available for APP disabler, cuz parcel cannot write ComponentName value
    // update: found in AIDL file
    fun setComponentRemote(name: Any, state: Int) {
        val data = SystemServiceHelper.obtainParcel("package",
                "android.content.pm.IPackageManager",
                "setApplicationEnabledSetting"
        )
        val reply = Parcel.obtain()
        data.also {
            when (name) {
                is String ->
                    it.writeString(name)
                is ComponentName ->
                    name.writeToParcel(it, 0)
                else ->
                    return
            }
            when (state) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED ->
                    it.writeInt(PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED ->
                    it.writeInt(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                else ->
                    it.writeInt(state)
            }
        }
        try {
            ShizukuService.transactRemote(data, reply, 0)
            reply.readException()
        } catch (tr: Throwable) {
            throw RuntimeException(tr.message, tr)
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    // method 1: use ShizukuBinderWrapper
    // TODO: complete the interface Class code
    fun setComponentWrapper(name: Any, state: Int, mIPackageManager: IPackageManager) {
        try {
            when (name) {
                is String -> mIPackageManager.setApplicationEnabledSetting(name, state, 0, 0)
                is ComponentName -> mIPackageManager.setComponentEnabledSetting(name, state, 0, 0)
                else -> return
            }
        } catch (tr: Throwable) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun getPackageManager(): IPackageManager {
        return IPackageManager.Stub.asInterface(
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
        )
    }

}
