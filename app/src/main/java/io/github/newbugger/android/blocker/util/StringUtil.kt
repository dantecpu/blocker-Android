package io.github.newbugger.android.blocker.util

import java.io.PrintWriter
import java.io.StringWriter

object StringUtil {

    @JvmStatic
    fun getStackTrace(e: Throwable): String {
        return StringWriter().let {
            e.printStackTrace(PrintWriter(it))
        }.toString()
    }

}
