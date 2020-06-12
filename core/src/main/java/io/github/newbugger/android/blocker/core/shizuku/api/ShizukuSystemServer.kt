package io.github.newbugger.android.blocker.core.shizuku.api

import android.content.pm.IPackageManager
import android.os.Parcel
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.SystemServiceHelper


object ShizukuSystemServer {

    fun getParcelData(): Parcel {
        return SystemServiceHelper.obtainParcel(
                "package",
                "android.content.pm.IPackageManager",
                "setApplicationEnabledSetting"
        )
    }

    fun getPackageManager(): IPackageManager {
        // Bug: system.err at here
        return IPackageManager.Stub.asInterface(
                ShizukuBinderWrapper(
                        SystemServiceHelper.getSystemService("package")
                )
        )
    }

}
