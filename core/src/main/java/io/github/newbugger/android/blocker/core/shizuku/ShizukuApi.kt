package io.github.newbugger.android.blocker.core.shizuku

import android.content.ComponentName
import android.content.pm.IPackageManager
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.ShizukuService
import moe.shizuku.api.SystemServiceHelper


object ShizukuApi {

    // method 2: use transactRemote directly
    fun setComponentRemote(name: String, state: Int) : Boolean {
        val data = SystemServiceHelper.obtainParcel("package",
                "android.content.pm.IPackageManager",
                "setComponentEnabledSetting"
        )
        val reply = Parcel.obtain()
        data.writeString(name)
        data.writeInt(state)
        return try{
            ShizukuService.transactRemote(data, reply, 0)
            reply.readException()
            true
        } catch (e: RemoteException) {
            Log.e("blocker.ShizukuApi", "IPackageManager#setApplicationEnabledSetting", e)
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    // method 1: use ShizukuBinderWrapper
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
