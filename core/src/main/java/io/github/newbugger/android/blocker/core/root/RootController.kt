package io.github.newbugger.android.blocker.core.root

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.util.Log
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.libsu.LibsuCommand


/**
 * Created by Mercury on 2017/12/31.
 * A class that controls the state of application components
 */

class RootController(val context: Context) : IController {
    private val tag = "io.github.newbugger.android.blocker.core.root.RootController"

    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        val comm: String = when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> removeEscapeCharacter(String.format(ENABLE_COMPONENT_TEMPLATE, packageName, componentName))
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> removeEscapeCharacter(String.format(DISABLE_COMPONENT_TEMPLATE, packageName, componentName))
            else -> return false
        }
        Log.d(tag, "command:$comm, componentState is $state")
        try {
            return LibsuCommand.code(LibsuCommand.command(comm))
        } catch (e: Exception) {
            throw e
        }
    }

    override fun enable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    override fun disable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    override fun batchEnable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        var succeededCount = 0
        componentList.forEach {
            if (enable(it.packageName, it.name)) {
                succeededCount++
            }
            action(it)
        }
        return succeededCount
    }

    override fun batchDisable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        var succeededCount = 0
        componentList.forEach {
            if (disable(it.packageName, it.name)) {
                succeededCount++
            }
            action(it)
        }
        return succeededCount
    }

    private fun removeEscapeCharacter(comm: String): String {
        return comm.replace("$", "\\$")
    }

    override fun checkComponentEnableState(packageName: String, componentName: String): Boolean {
        return ApplicationUtil.checkComponentIsEnabled(context.packageManager, ComponentName(packageName, componentName))
    }

    companion object {
        private const val DISABLE_COMPONENT_TEMPLATE = "pm disable %s/%s"
        private const val ENABLE_COMPONENT_TEMPLATE = "pm enable %s/%s"
    }

}
