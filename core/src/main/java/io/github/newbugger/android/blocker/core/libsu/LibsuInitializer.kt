package io.github.newbugger.android.blocker.core.libsu

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell


class LibsuInitializer: Shell.Initializer() {

    override fun onInit(context: Context, shell: Shell): Boolean {
        Log.d("io.github.newbugger.android.blocker.core.libsu.LibsuInitializer", "onInit")
        return true
    }

}
