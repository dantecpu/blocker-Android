package io.github.newbugger.android.blocker

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell
import io.github.newbugger.android.blocker.libsu.LibsuInitializer
import io.github.newbugger.android.blocker.util.BuildUtil


class BlockerApplication : Application() {

    init {
        Shell.setDefaultBuilder((Shell.Builder.create()
                .apply {
                    if (BuildUtil.BuildProperty.isTimeOut()) {
                        setTimeout(10)
                    }
                }
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setInitializers(LibsuInitializer::class.java)).also {
                    if (BuildUtil.BuildProperty.isBuildDebug()) {
                        Shell.enableVerboseLogging = BuildConfig.DEBUG
                    }
                }
        )
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        lateinit var context: Context
    }

}
