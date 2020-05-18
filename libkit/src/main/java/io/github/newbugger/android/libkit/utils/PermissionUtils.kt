package io.github.newbugger.android.libkit.utils

import com.stericson.RootTools.RootTools

object PermissionUtils {
    val isRootAvailable: Boolean
        get() = RootTools.isRootAvailable()
}
