package io.github.newbugger.android.blocker.core.shizuku.api

import android.content.pm.IPackageManager
import android.os.Parcel
import io.github.newbugger.android.blocker.core.shizuku.util.Singleton
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.SystemServiceHelper


object ShizukuSystemServer {

    // Parcel should not and can not be cached using Singleton
    fun getParcelData(): Parcel {
        return SystemServiceHelper.obtainParcel(
                "package",
                "android.content.pm.IPackageManager",
                "setApplicationEnabledSetting"
        )
    }

    fun getPackageManager(): IPackageManager {
        return pm.get()
    }

    private val pm = object : Singleton<IPackageManager>() {
        override fun create(): IPackageManager {
            return IPackageManager.Stub.asInterface(
                    ShizukuBinderWrapper(
                            SystemServiceHelper.getSystemService("package")
                    )
            )
        }
    }

}
