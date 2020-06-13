package io.github.newbugger.android.blocker.shizuku

import android.util.Log


class ShizukuProvider : moe.shizuku.api.ShizukuProvider() {

    override fun onCreate(): Boolean {
        Log.d("io.github.newbugger.android.blocker.shizuku",
                "ShizukuProvider: onShizukuProvider")
        return super.onCreate()
    }

}
