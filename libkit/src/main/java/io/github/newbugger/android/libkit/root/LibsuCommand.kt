package io.github.newbugger.android.libkit.root

import android.util.Log
import com.topjohnwu.superuser.Shell
import java.io.IOException


object LibsuCommand {

    fun check() {
        if (!Shell.rootAccess())
            throw RootUnavailableException()
    }

    fun command(comm: String): Shell.Result {
        return try {
            val result = Shell.su(comm).exec()
            Log.d("io.github.newbugger.android.libkit.root.LibsuCommand", result.out.joinToString(separator = "\n"))
            result
        } catch (tr: Throwable) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun code(u: Shell.Result): Boolean {
        return u.isSuccess
    }

    fun output(u: Shell.Result): List<String> {
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
