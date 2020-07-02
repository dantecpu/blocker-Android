package io.github.newbugger.android.blocker.libsu

import android.content.Context
import com.topjohnwu.superuser.Shell


class LibsuInitializer: Shell.Initializer() {

    override fun onInit(context: Context, shell: Shell): Boolean {
        // Log.d(javaClass.name, "LibsuInitializer: onInit")
        return true
    }

}
