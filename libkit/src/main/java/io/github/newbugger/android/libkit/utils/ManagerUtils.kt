package io.github.newbugger.android.libkit.utils

import io.github.newbugger.android.libkit.RootCommand


object ManagerUtils {

    fun forceStop(packageName: String) {
        RootCommand.runBlockingCommand("am force-stop $packageName")
    }

    /*fun startService(packageName: String, serviceName: String) {
        RootCommand.runBlockingCommand("am startservice $packageName/$serviceName")
    }*/

    /*fun stopService(packageName: String, serviceName: String) {
        RootCommand.runBlockingCommand("am stopservice $packageName/$serviceName")
    }*/

    fun disableApplication(packageName: String) {
        RootCommand.runBlockingCommand("pm disable $packageName")
    }

    fun enableApplication(packageName: String) {
        RootCommand.runBlockingCommand("pm enable $packageName")
    }

}
