package io.github.newbugger.android.blocker.core.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.libkit.utils.ApplicationUtil


class ShizukuController(val context: Context) : IController {

    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        // TODO: temporarily use one transact method
        // TODO: always disable the whole package when on shizuku service
        /*if (componentName != null) {
            ShizukuApi.setComponentWrapper(packageName, componentName, state)
            ShizukuApi.setComponentRemote(packageName, componentName, state)
        }
        else {
            ShizukuApi.setComponentWrapper(packageName, null, state)
            ShizukuApi.setComponentRemote(packageName, null, state)
        }*/
        ShizukuApi.setComponentRemote(packageName, null, state)
        return true
    }

    override fun enable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    override fun disable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    override fun batchEnable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        var successCount = 0
        componentList.forEach {
            if (enable(it.packageName, it.name)) {
                successCount++
            }
            action(it)
        }
        return successCount
    }

    override fun batchDisable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        var successCount = 0
        componentList.forEach {
            if (disable(it.packageName, it.name)) {
                successCount++
            }
            action(it)
        }
        return successCount
    }

    override fun checkComponentEnableState(packageName: String, componentName: String): Boolean {
        return ApplicationUtil.checkComponentIsEnabled(context.packageManager, ComponentName(packageName, componentName))
    }

    override fun checkPackageEnableState(packageName: String): Boolean {
        return ApplicationUtil.checkPackageIsEnabled(context.packageManager, packageName)
    }

}
