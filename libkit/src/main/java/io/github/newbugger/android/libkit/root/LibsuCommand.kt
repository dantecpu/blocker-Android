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
        try {
            check()
            return Shell.su(comm).exec().also {
                Log.d("io.github.newbugger.android.libkit.root.LibsuCommand",
                        it.out.joinToString(separator = ", "))
            }
        } catch (e: Exception) {
            throw Exception(e)
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

    /*fun test(): String {
        // list<String>.toString == [test,test,test,test]
        return Shell.sh("echo -e 'test\ntest\ntest\ntest'").exec().out.toString()
    }*/

}
