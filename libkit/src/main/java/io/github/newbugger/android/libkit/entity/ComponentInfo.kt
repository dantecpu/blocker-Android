package io.github.newbugger.android.libkit.entity

import android.content.pm.ComponentInfo

fun ComponentInfo.getSimpleName(): String {
    return name.split(".").last()
}
