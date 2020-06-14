package io.github.newbugger.android.blocker.shizuku

import android.util.Log


class ShizukuProvider : moe.shizuku.api.ShizukuProvider() {

    override fun onCreate(): Boolean {
        Log.d(javaClass.name,
                "ShizukuProvider: onShizukuProvider")
        return super.onCreate()
    }

}
