package io.github.newbugger.android.blocker.core.ifw

import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.github.newbugger.android.blocker.core.ComponentControllerProxy
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.blocker.core.root.EControllerMethod
import io.github.newbugger.android.ifw.IntentFirewall
import io.github.newbugger.android.ifw.IntentFirewallImpl
import io.github.newbugger.android.ifw.entity.ComponentType
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import java.lang.Exception


class IfwController(val context: Context) : IController {
    private lateinit var controller: IntentFirewall
    private lateinit var packageInfo: PackageInfo

    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        init(packageName)
        val type = getComponentType(componentName)
        if (type == ComponentType.PROVIDER) {
            return when (state) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> ComponentControllerProxy.getInstance(EControllerMethod.PM, context).disable(packageName, componentName)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> ComponentControllerProxy.getInstance(EControllerMethod.PM, context).enable(packageName, componentName)
                else -> false
            }
        }
        val result = when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> controller.add(packageName, componentName, type)
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> controller.remove(packageName, componentName, type)
            else -> false
        }
        if (result) {
            try {
                controller.save()
            } catch (e: Exception) {
                controller.removeCache()
                throw e
            }
        }
        return result
    }

    override fun enable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    override fun disable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    override fun batchEnable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        var succeededCount = 0
        if (componentList.isEmpty()) {
            return succeededCount
        }
        componentList.forEach {
            init(it.packageName)
            val type = getComponentType(it.name)
            if (controller.remove(it.packageName, it.name, type)) {
                succeededCount++
            }
            action(it)
        }
        controller.save()
        return succeededCount
    }

    override fun batchDisable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        var succeededCount = 0
        if (componentList.isEmpty()) {
            return succeededCount
        }
        componentList.forEach {
            init(it.packageName)
            val type = getComponentType(it.name)
            if (controller.add(it.packageName, it.name, type)) {
                succeededCount++
            }
            action(it)
        }
        controller.save()
        return succeededCount
    }

    override fun checkComponentEnableState(packageName: String, componentName: String): Boolean {
        init(packageName)
        return controller.getComponentEnableState(packageName, componentName)
    }

    override fun checkPackageEnableState(packageName: String): Boolean {
        // fake body
        init(packageName)
        return controller.getPackageEnableState(packageName)
    }

    private fun init(packageName: String) {
        initController(packageName)
        initPackageInfo(packageName)
    }

    private fun initController(packageName: String) {
        if (!::controller.isInitialized || controller.packageName != packageName) {
            controller = IntentFirewallImpl.getInstance(context, packageName)
            return
        }
    }

    private fun initPackageInfo(packageName: String) {
        if (!::packageInfo.isInitialized || packageInfo.packageName != packageName) {
            packageInfo = ApplicationUtil.getApplicationComponents(context.packageManager, packageName)
        }
    }

    private fun getComponentType(componentName: String): ComponentType {
        packageInfo.receivers?.forEach {
            if (it.name == componentName) {
                return ComponentType.BROADCAST
            }
        }
        packageInfo.services?.forEach {
            if (it.name == componentName) {
                return ComponentType.SERVICE
            }
        }
        packageInfo.activities?.forEach {
            if (it.name == componentName) {
                return ComponentType.ACTIVITY
            }
        }
        packageInfo.providers?.forEach {
            if (it.name == componentName) {
                return ComponentType.PROVIDER
            }
        }
        return ComponentType.UNKNOWN
    }

}
