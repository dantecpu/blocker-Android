package io.github.newbugger.android.blocker.core.shizuku

import android.content.pm.IPackageManager
import io.github.newbugger.android.blocker.core.shizuku.util.Singleton
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.SystemServiceHelper


object ShizukuSystemServer {

    fun getPackageManager(): Singleton<IPackageManager> {
        return pm
    }

    private val pm = object : Singleton<IPackageManager>() {
        override fun create(): IPackageManager {
            return IPackageManager.Stub.asInterface(
                    ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
            )
        }
    }

}
