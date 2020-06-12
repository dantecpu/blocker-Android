package io.github.newbugger.android.libkit.utils

import io.github.newbugger.android.libkit.libsu.LibsuCommand


object ManagerUtils {

    fun forceStop(packageName: String) {
        LibsuCommand.command("am force-stop $packageName")
    }

    /*fun startService(packageName: String, serviceName: String) {
        LibsuCommand.command("am startservice $packageName/$serviceName")
    }

    fun stopService(packageName: String, serviceName: String) {
        LibsuCommand.command("am stopservice $packageName/$serviceName")
    }*/

    fun disableApplication(packageName: String) {
        LibsuCommand.command("pm disable $packageName")
    }

    fun enableApplication(packageName: String) {
        LibsuCommand.command("pm enable $packageName")
    }

}
