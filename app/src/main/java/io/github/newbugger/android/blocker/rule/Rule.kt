package io.github.newbugger.android.blocker.rule

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.newbugger.android.blocker.core.ComponentControllerProxy
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.blocker.core.prescription.PrescriptionUtil
import io.github.newbugger.android.blocker.core.root.EControllerMethod
import io.github.newbugger.android.blocker.rule.entity.BlockerRule
import io.github.newbugger.android.blocker.rule.entity.ComponentRule
import io.github.newbugger.android.blocker.rule.entity.RulesResult
import io.github.newbugger.android.blocker.ui.component.EComponentType
import io.github.newbugger.android.blocker.util.storage.ModernStorageLocalUtil
import io.github.newbugger.android.blocker.util.PreferenceUtil
import io.github.newbugger.android.ifw.IntentFirewallImpl
import io.github.newbugger.android.ifw.entity.ComponentType
import io.github.newbugger.android.ifw.util.RuleSerializer
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.libkit.utils.FileUtils
import io.github.newbugger.android.libkit.utils.StorageUtils
import io.github.newbugger.android.storage.directfileaccess.DirectFileUtil.getExternalDirectory
import java.io.File
import java.io.FileWriter


object Rule {

    // over Blocker rule mode, both IFW and PM are applied
    fun export(context: Context, packageName: String): RulesResult {
        val pm = context.packageManager
        val applicationInfo = ApplicationUtil.getApplicationComponents(pm, packageName)
        val rule = BlockerRule(packageName = applicationInfo.packageName, versionName = applicationInfo.versionName, versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) applicationInfo.longVersionCode.toInt() else applicationInfo.versionCode)
        var disabledComponentsCount = 0
        val ifwController = IntentFirewallImpl.getInstance(context, packageName)
        applicationInfo.receivers?.forEach {
            val stateIFW = ifwController.getComponentEnableState(it.packageName, it.name)
            val statePM = ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))
            rule.components.add(ComponentRule(it.name, stateIFW, EComponentType.RECEIVER, EControllerMethod.IFW))
            rule.components.add(ComponentRule(it.name, statePM, EComponentType.RECEIVER, EControllerMethod.PM))
            disabledComponentsCount++
        }
        applicationInfo.services?.forEach {
            val stateIFW = ifwController.getComponentEnableState(it.packageName, it.name)
            val statePM = ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))
            rule.components.add(ComponentRule(it.name, stateIFW, EComponentType.SERVICE, EControllerMethod.IFW))
            rule.components.add(ComponentRule(it.name, statePM, EComponentType.SERVICE, EControllerMethod.PM))
            disabledComponentsCount++
        }
        applicationInfo.activities?.forEach {
            val stateIFW = ifwController.getComponentEnableState(it.packageName, it.name)
            val statePM = ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))
            rule.components.add(ComponentRule(it.name, stateIFW, EComponentType.ACTIVITY, EControllerMethod.IFW))
            rule.components.add(ComponentRule(it.name, statePM, EComponentType.ACTIVITY, EControllerMethod.PM))
            disabledComponentsCount++
        }
        applicationInfo.providers?.forEach {
            val statePM = ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))
            rule.components.add(ComponentRule(it.name, statePM, EComponentType.PROVIDER, EControllerMethod.PM))
            disabledComponentsCount++
        }
        return if (rule.components.isNotEmpty()) {
            saveRuleToStorage(context, packageName, rule)
            RulesResult(true, disabledComponentsCount, 0)
        } else {
            RulesResult(false, 0, 0)
        }
    }

    // over Blocker rule mode, both IFW and PM are applied
    fun import(context: Context, file: String?): RulesResult {
        val appRule = Gson().fromJson<BlockerRule>(file, BlockerRule::class.java) ?: return RulesResult(false, 0, 0)
        var succeedCount = 0
        var failedCount = 0
        val total = appRule.components.size
        val controller = getController(context)
        val ifwController = IntentFirewallImpl.getInstance(context, appRule.packageName)
        try {
            appRule.components.forEach {
                val controllerResult = when (it.method) {
                    EControllerMethod.IFW -> {
                        when (it.type) {
                            EComponentType.RECEIVER -> {
                                if (it.enabled) {
                                    (ifwController?.add(appRule.packageName, it.name, ComponentType.BROADCAST)).also {
                                        ifwController?.save()
                                    } ?: false
                                } else {
                                    (ifwController?.remove(appRule.packageName, it.name, ComponentType.BROADCAST)).also {
                                        ifwController?.save()
                                    } ?: false
                                }
                            }
                            EComponentType.SERVICE -> {
                                if (it.enabled) {
                                    (ifwController?.add(appRule.packageName, it.name, ComponentType.SERVICE)).also {
                                        ifwController?.save()
                                    } ?: false
                                } else {
                                    (ifwController?.remove(appRule.packageName, it.name, ComponentType.SERVICE)).also {
                                        ifwController?.save()
                                    } ?: false
                                }
                            }
                            EComponentType.ACTIVITY -> {
                                if (it.enabled) {
                                    (ifwController?.add(appRule.packageName, it.name, ComponentType.ACTIVITY)).also {
                                        ifwController?.save()
                                    } ?: false
                                } else {
                                    (ifwController?.remove(appRule.packageName, it.name, ComponentType.ACTIVITY)).also {
                                        ifwController?.save()
                                    } ?: false
                                }
                            }
                            // content provider needs PM to implement it
                            EComponentType.PROVIDER -> {
                                if (it.enabled) {
                                    controller.enable(appRule.packageName, it.name)
                                } else {
                                    controller.disable(appRule.packageName, it.name)
                                }
                            }
                            EComponentType.UNKNOWN -> false
                        }
                    }
                    EControllerMethod.PM -> {
                        if (it.enabled) {
                            controller.enable(appRule.packageName, it.name)
                        } else {
                            controller.disable(appRule.packageName, it.name)
                        }
                    }
                }
                if (controllerResult) {
                    succeedCount++
                } else {
                    failedCount++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return RulesResult(false, succeedCount, failedCount)
        }
        return RulesResult(true, succeedCount, failedCount)
    }

    // over Ifw rule mode, only three components are exported
    fun exportIfwRules(context: Context): Int {
        val ifwFolder = StorageUtils.getIfwFolder()
        val ifwBackupFolder = getBlockerIFWFolder(context)
        /*FileUtils.listFiles(ifwBackupFolder).forEach { f->
            val filename = f.split(File.separator).last()
            val content = FileUtils.read(f)
            FileWriter(File(ifwBackupFolder, filename)).apply {
                write(content)
                close()
            }
        }*/
        FileUtils.copy(ifwFolder, ifwBackupFolder)
        FileUtils.listFiles(ifwBackupFolder).filter {
            // remove the Greenify cut-off configs
            it.split(File.separator).last() == "gib.xml" || it.split(File.separator).last() == "grx.xml"
        }.forEach {
            File(it).delete()
        }
        return FileUtils.count(ifwBackupFolder)
    }

    // over Ifw rule mode, only three components are exported
    fun importIfwRules(context: Context): Int {
        val controller = ComponentControllerProxy.getInstance(EControllerMethod.IFW, context)
        var succeedCount = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ModernStorageLocalUtil.readAllText(context, ConstantUtil.NAME_RULE_IFW, ModernStorageLocalUtil.mimeTypeXml).forEach { (packageName, text) ->
                if (packageName == null || text == null) return@forEach
                if (!isApplicationInstalled(context, packageName)) return@forEach
                val rule = RuleSerializer.deserialize(text) ?: return@forEach
                val activities = rule.activity?.componentFilters
                        ?.asSequence()
                        ?.map { filter -> filter.name.split("/") }
                        ?.map { names ->
                            val component = ComponentInfo()
                            component.packageName = names[0]
                            component.name = names[1]
                            component
                        }
                        ?.toList() ?: mutableListOf()
                val broadcast = rule.broadcast?.componentFilters
                        ?.asSequence()
                        ?.map { filter -> filter.name.split("/") }
                        ?.map { names ->
                            val component = ComponentInfo()
                            component.packageName = names[0]
                            component.name = names[1]
                            component
                        }
                        ?.toList() ?: mutableListOf()
                val service = rule.service?.componentFilters
                        ?.asSequence()
                        ?.map { filter -> filter.name.split("/") }
                        ?.map { names ->
                            val component = ComponentInfo()
                            component.packageName = names[0]
                            component.name = names[1]
                            component
                        }
                        ?.toList() ?: mutableListOf()
                controller.batchDisable(activities) { }
                controller.batchDisable(broadcast) { }
                controller.batchDisable(service) { }
                succeedCount++
            }
        } else {
            val ifwBackupFolder = getBlockerIFWFolder(context)
            FileUtils.listFiles(ifwBackupFolder).filter {
                it.endsWith(ConstantUtil.EXTENSION_XML) && isApplicationInstalled(context, it.replace(ConstantUtil.EXTENSION_XML, ""))
            }.forEach {
                val f = File(it)
                val rule = RuleSerializer.deserialize(f) ?: return@forEach
                val activities = rule.activity?.componentFilters
                        ?.asSequence()
                        ?.map { filter -> filter.name.split("/") }
                        ?.map { names ->
                            val component = ComponentInfo()
                            component.packageName = names[0]
                            component.name = names[1]
                            component
                        }
                        ?.toList() ?: mutableListOf()
                val broadcast = rule.broadcast?.componentFilters
                        ?.asSequence()
                        ?.map { filter -> filter.name.split("/") }
                        ?.map { names ->
                            val component = ComponentInfo()
                            component.packageName = names[0]
                            component.name = names[1]
                            component
                        }
                        ?.toList() ?: mutableListOf()
                val service = rule.service?.componentFilters
                        ?.asSequence()
                        ?.map { filter -> filter.name.split("/") }
                        ?.map { names ->
                            val component = ComponentInfo()
                            component.packageName = names[0]
                            component.name = names[1]
                            component
                        }
                        ?.toList() ?: mutableListOf()
                controller.batchDisable(activities) { }
                controller.batchDisable(broadcast) { }
                controller.batchDisable(service) { }
                succeedCount++
            }
        }
        return succeedCount
    }

    fun resetIfw(): Boolean {
        var result = true
        try {
            val ifwFolder = StorageUtils.getIfwFolder()
            val files = FileUtils.listFiles(ifwFolder)
            files.forEach {
                if (!FileUtils.delete(it, false)) {
                    result = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    fun exportPrescription(context: Context,
                           packageName: String, className: String,
                           typeC: String, sender: String,
                           action: String?, cat: String?,
                           typeF: String?, scheme: String?,
                           auth: String?, path: String?, pathOption: String?): Int {
        val prescriptionFolder = getBlockerPrescriptionFolder(context)
        val content = PrescriptionUtil.template(
                packageName, className, typeC, sender, action, cat, typeF, scheme, auth, path, pathOption)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val written = "${PrescriptionUtil.head()}\n${PrescriptionUtil.header()}\n${content}\n${PrescriptionUtil.footer()}"
            ModernStorageLocalUtil.writeText(context, written, ConstantUtil.NAME_RULE_PRESCRIPTION, packageName)
        } else {
            FileWriter(File(prescriptionFolder, packageName + ConstantUtil.EXTENSION_XML)).apply {
                write(PrescriptionUtil.head())
                write(PrescriptionUtil.header())
                write(content)
                write(PrescriptionUtil.footer())
                close()
            }
        }
        return 1
    }

    private fun saveRuleToStorage(context: Context, packageName: String, rule: BlockerRule) {
        val json: String = GsonBuilder().setPrettyPrinting().create().toJson(rule)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ModernStorageLocalUtil.writeText(context, json, ConstantUtil.NAME_RULE_BLOCKER, packageName)
        } else {
            File(getBlockerRuleFolder(context), packageName + ConstantUtil.EXTENSION_JSON).let {
                if (it.exists()) it.delete()
                it.writeText(json)
            }
        }
    }

    private fun isApplicationInstalled(context: Context, packageName: String): Boolean {
        return ApplicationUtil.isAppInstalled(context.packageManager, packageName)
    }

    fun getBlockerRuleFolder(context: Context): String =
            context.getExternalDirectory(ConstantUtil.NAME_RULE_BLOCKER)

    private fun getBlockerIFWFolder(context: Context): String =
            context.getExternalDirectory(ConstantUtil.NAME_RULE_IFW)

    private fun getBlockerPrescriptionFolder(context: Context): String =
            context.getExternalDirectory(ConstantUtil.NAME_RULE_PRESCRIPTION)

    private fun getController(context: Context): IController {
        val controllerType = PreferenceUtil.getControllerType(context)
        return ComponentControllerProxy.getInstance(controllerType, context)
    }

}
