package io.github.newbugger.android.blocker.libsu

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell


class LibsuInitializer: Shell.Initializer() {

    override fun onInit(context: Context, shell: Shell): Boolean {
        Log.d("io.github.newbugger.android.blocker.libsu.LibsuInitializer", "onInit")
        return true
    }

}