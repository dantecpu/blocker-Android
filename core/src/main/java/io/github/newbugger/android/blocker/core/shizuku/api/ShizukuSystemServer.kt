package io.github.newbugger.android.blocker.core.shizuku.api

import android.content.pm.IPackageManager
import android.os.Parcel
import io.github.newbugger.android.blocker.core.shizuku.util.Singleton
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.SystemServiceHelper


object ShizukuSystemServer {

    fun getParcelData(): Parcel {
        return pr.get()
    }

    fun getPackageManager(): IPackageManager {
        return pm.get()
    }

    private val pr = object : Singleton<Parcel>() {
        // Bug: call for only once ?
        override fun create(): Parcel {
            return SystemServiceHelper.obtainParcel(
                    "package",
                    "android.content.pm.IPackageManager",
                    "setApplicationEnabledSetting"
            )
        }
    }

    private val pm = object : Singleton<IPackageManager>() {
        // Bug: system.err at here
        override fun create(): IPackageManager {
            return IPackageManager.Stub.asInterface(
                    ShizukuBinderWrapper(
                            SystemServiceHelper.getSystemService("package")
                    )
            )
        }
    }

}
