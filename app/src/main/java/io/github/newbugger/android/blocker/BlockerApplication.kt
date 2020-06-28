package io.github.newbugger.android.blocker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.Shell
import io.github.newbugger.android.blocker.libsu.LibsuInitializer


class BlockerApplication : Application() {

    // from: topjohnwu/Magisk/blob/master/app/src/main/java/com/topjohnwu/magisk/core/App.kt
    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setTimeout(10)
        Shell.Config.setFlags(Shell.FLAG_USE_MAGISK_BUSYBOX)
        Shell.Config.addInitializers(LibsuInitializer::class.java)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        context = this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

}
