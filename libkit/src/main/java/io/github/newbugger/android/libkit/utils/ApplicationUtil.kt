package io.github.newbugger.android.libkit.utils

import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.newbugger.android.libkit.entity.Application
import java.util.*

/**
 * Created by Mercury on 2017/12/30.
 * A class that gets activities, broadcasts, content providers, and services
 */


object ApplicationUtil {

    /**
     * Get a list of installed applications on device
     *
     * @param context Context
     * @return list of application info
     */
    fun getApplicationList(context: Context): MutableList<Application> {
        val pm = context.packageManager
        return pm.getInstalledPackages(0)
                .asSequence()
                .map {
                    Application(pm, it)
                }
                .toMutableList()
    }

    /**
     * get a list of installed third party applications
     *
     * @param context Context
     * @return a list of installed third party applications
     */
    fun getThirdPartyApplicationList(context: Context): MutableList<Application> {
        val pm = context.packageManager
        return pm.getInstalledPackages(0)
                .asSequence()
                .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .map {
                    Application(pm, it)
                }
                .toMutableList()
    }

    /**
     * get a list of system applications
     *
     * @param context Context
     * @return a list of installed system applications
     */
    fun getSystemApplicationList(context: Context): MutableList<Application> {
        val pm = context.packageManager
        return pm.getInstalledPackages(0)
                .asSequence()
                .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
                .map {
                    Application(pm, it)
                }
                .toMutableList()
    }

    fun getGoogleSystemApplicationList(context: Context): MutableList<Application> {
        val pm = context.packageManager
        return pm.getInstalledPackages(0)
                .asSequence()
                .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
                .filter { it.packageName.startsWith("com.google.") }
                .map {
                    Application(pm, it)
                }
                .toMutableList()
    }

    /**
     * get a list of activity of a specified application
     *
     * @param pm          PackageManager
     * @param packageName package name
     * @return list of activity
     */
    @Suppress("DEPRECATION")
    fun getActivityList(pm: PackageManager, packageName: String): MutableList<ActivityInfo> {
        val activities = ArrayList<ActivityInfo>()
        try {
            var flags = PackageManager.GET_ACTIVITIES
            flags = flags or PackageManager.MATCH_DISABLED_COMPONENTS
            val components = pm.getPackageInfo(packageName, flags).activities
            if (components != null && components.isNotEmpty()) {
                Collections.addAll(activities, *components)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            return ApkUtils.getActivities(pm, packageName)
        }
        return activities
    }

    /**
     * get a list of receiver of a specified application
     *
     * @param pm          PackageManager
     * @param packageName package name
     * @return list of receiver
     */
    @Suppress("DEPRECATION")
    fun getReceiverList(pm: PackageManager, packageName: String): MutableList<ActivityInfo> {
        val receivers = ArrayList<ActivityInfo>()
        try {
            var flags = PackageManager.GET_RECEIVERS
            flags = flags or PackageManager.MATCH_DISABLED_COMPONENTS
            val components = pm.getPackageInfo(packageName, flags).receivers
            if (components != null && components.isNotEmpty()) {
                Collections.addAll(receivers, *components)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return receivers
    }

    /**
     * get a list of service of a specified application
     *
     * @param pm          PackageManager
     * @param packageName package name
     * @return list of service
     */
    fun getServiceList(pm: PackageManager, packageName: String): MutableList<ServiceInfo> {
        val services = ArrayList<ServiceInfo>()
        try {
            var flags = PackageManager.GET_SERVICES
            flags = flags or PackageManager.MATCH_DISABLED_COMPONENTS
            val components = pm.getPackageInfo(packageName, flags).services
            if (components != null && components.isNotEmpty()) {
                Collections.addAll(services, *components)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return services
    }

    /**
     * get a list of service of a specified application
     *
     * @param pm          PackageManager
     * @param packageName package name
     * @return list of provider
     */
    @Suppress("DEPRECATION")
    fun getProviderList(pm: PackageManager, packageName: String): MutableList<ProviderInfo> {
        val providers = ArrayList<ProviderInfo>()
        try {
            var flags = PackageManager.GET_PROVIDERS
            flags = flags or PackageManager.MATCH_DISABLED_COMPONENTS
            val components = pm.getPackageInfo(packageName, flags).providers
            if (components != null && components.isNotEmpty()) {
                Collections.addAll(providers, *components)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return providers
    }

    fun getApplicationInfo(context: Context, packageName: String): Application? {
        val pm = context.packageManager
        val info = pm.getPackageInfo(packageName, 0) ?: return null
        return Application(pm, info)
    }

    /**
     * get a list of components of a specified application
     *
     * @param pm          PackageManager
     * @param packageName package name
     * @return a set of components
     */
    fun getApplicationComponents(pm: PackageManager, packageName: String): PackageInfo {
        var flags = PackageManager.GET_ACTIVITIES or PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or PackageManager.GET_SERVICES or
                PackageManager.GET_INTENT_FILTERS
        flags = flags or PackageManager.MATCH_DISABLED_COMPONENTS
        var info = PackageInfo()
        try {
            info = pm.getPackageInfo(packageName, flags)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            info = getPackageInfoFromManifest(pm, packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return info
    }

    private fun getPackageInfoFromManifest(pm: PackageManager, packageName: String): PackageInfo {
        val info = PackageInfo()
        info.packageName = packageName
        info.activities = getActivityList(pm, packageName).toTypedArray()
        info.services = getServiceList(pm, packageName).toTypedArray()
        info.receivers = getReceiverList(pm, packageName).toTypedArray()
        info.providers = getProviderList(pm, packageName).toTypedArray()
        return info
    }

    /**
     * check a component is enabled or not
     *
     * @param pm            PackageManager
     * @param componentName name of a component
     * @return true : component is enabled , false: component is disabled
     */
    fun checkComponentIsEnabled(pm: PackageManager, componentName: ComponentName): Boolean {
        val state: Int
        try {
            state = pm.getComponentEnabledSetting(componentName)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    }

    /**
     * check an application is installed or not
     *
     * @param pm PackageManager
     * @return true : component is enabled , false: component is disabled
     */
    fun isAppInstalled(pm: PackageManager, packageName: String): Boolean {
        try {
            pm.getApplicationInfo(packageName, 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    private fun getBlockedApplication(context: Context): MutableList<String> {
        val sharedPreferences = context.getSharedPreferences(ConstantUtil.BLOCKED_CONF_NAME, MODE_PRIVATE)
        val json = sharedPreferences.getString(ConstantUtil.BLOCKED_APP_LIST_KEY, "[]")
        return Gson().fromJson(json, object : TypeToken<MutableList<String>>() {}.type)
    }

    private fun saveBlockedApplication(context: Context, applications: List<String>) {
        val editor = context.getSharedPreferences(ConstantUtil.BLOCKED_CONF_NAME, MODE_PRIVATE).edit()
        editor.putString(ConstantUtil.BLOCKED_APP_LIST_KEY, Gson().toJson(applications))
        editor.apply()
    }

    fun addBlockedApplication(context: Context, packageName: String) {
        val blockedApplication = getBlockedApplication(context)
        if (blockedApplication.contains(packageName)) {
            return
        }
        blockedApplication.add(packageName)
        saveBlockedApplication(context, blockedApplication)
    }

    fun removeBlockedApplication(context: Context, packageName: String) {
        val blockedApplication = getBlockedApplication(context)
        blockedApplication.remove(packageName)
        saveBlockedApplication(context, blockedApplication)
    }

}
