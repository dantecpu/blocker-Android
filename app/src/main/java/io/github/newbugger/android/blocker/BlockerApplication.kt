package io.github.newbugger.android.blocker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.Shell
import io.github.newbugger.android.blocker.libsu.LibsuInitializer
import me.weishu.reflection.Reflection


class BlockerApplication : Application() {

    // from: topjohnwu/Magisk/blob/master/app/src/main/java/com/topjohnwu/magisk/core/App.kt
    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setTimeout(10)
        Shell.Config.addInitializers(LibsuInitializer::class.java)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        context = this
        Reflection.unseal(this) // the dependency of shizuku as to keep there
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val LOG_FILENAME = "blocker_log.log"
    }

}
