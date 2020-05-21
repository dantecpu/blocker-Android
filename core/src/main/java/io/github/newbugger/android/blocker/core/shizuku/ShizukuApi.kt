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
    fun setPackageRemote(name: String, state: Int) {
        val data = SystemServiceHelper.obtainParcel("package",
                "android.content.pm.IPackageManager",
                "setApplicationEnabledSetting"
        )
        val reply = Parcel.obtain()
        data.apply {
            writeString(name)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                writeInt(PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
            else
                writeInt(state)
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
            if (name is ComponentName)
                mIPackageManager.setComponentEnabledSetting(name, state, 0, 0)
            else if (name is String)
                mIPackageManager.setApplicationEnabledSetting(name, state, 0, 0)
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
