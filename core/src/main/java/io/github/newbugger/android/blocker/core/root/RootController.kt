package io.github.newbugger.android.blocker.core.root

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.libsu.LibsuCommand
import io.github.newbugger.android.libkit.utils.ConstantUtil


/**
 * Created by Mercury on 2017/12/31.
 * A class that controls the state of application components
 */

class RootController(val context: Context) : IController {

    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        val comm: String = when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> removeEscapeCharacter(String.format(ConstantUtil.COMMAND_ENABLE_COMPONENT, packageName, componentName))
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> removeEscapeCharacter(String.format(ConstantUtil.COMMAND_DISABLE_COMPONENT, packageName, componentName))
            else -> return false
        }
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

}
