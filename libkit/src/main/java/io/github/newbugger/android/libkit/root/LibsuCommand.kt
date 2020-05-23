package io.github.newbugger.android.libkit.root

import android.util.Log
import com.topjohnwu.superuser.Shell
import java.io.IOException


object LibsuCommand {

    private fun check() {
        if (!Shell.rootAccess())
            throw RootUnavailableException()
    }

    fun command(comm: String): Shell.Result {
        return try {
            check()
            val result = Shell.su(comm).exec()
            Log.d("io.github.newbugger.android.libkit.root.LibsuCommand", result.out.joinToString(separator = "\n"))
            result
        } catch (tr: Throwable) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun code(u: Shell.Result): Boolean {
        check()
        return u.isSuccess
    }

    fun output(u: Shell.Result): List<String> {
        check()
        return u.out
    }

    fun close() {
        try {
            Shell.getCachedShell()?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
