package io.github.newbugger.android.blocker.ui.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import moe.shizuku.api.ShizukuService


class ShizukuBinderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("io.github.newbugger.android.blocker.ui.component.ShizukuBinderReceiver",
                "onReceive binder: " + ShizukuService.getBinder()
        )
    }

}
