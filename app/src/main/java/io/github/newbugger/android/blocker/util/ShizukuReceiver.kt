package io.github.newbugger.android.blocker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import moe.shizuku.api.ShizukuService


class ShizukuReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("io.github.newbugger.android.blocker.util.ShizukuReceiver",
                "onReceive binder: " + ShizukuService.getBinder()
        )
    }

}
