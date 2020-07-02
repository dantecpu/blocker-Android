package io.github.newbugger.android.blocker.rule

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import io.github.newbugger.android.blocker.core.ComponentControllerProxy
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.blocker.core.root.EControllerMethod
import io.github.newbugger.android.blocker.rule.entity.BlockerRule
import io.github.newbugger.android.blocker.rule.entity.ComponentRule
import io.github.newbugger.android.blocker.rule.entity.RulesResult
import io.github.newbugger.android.blocker.ui.component.EComponentType
import io.github.newbugger.android.blocker.util.PreferenceUtil
import io.github.newbugger.android.ifw.IntentFirewall
import io.github.newbugger.android.ifw.IntentFirewallImpl
import io.github.newbugger.android.ifw.entity.ComponentType
import io.github.newbugger.android.ifw.util.RuleSerializer
import io.github.newbugger.android.libkit.utils.*
import java.io.File
import java.io.FileWriter
import java.io.FileReader
import java.io.IOException
import java.lang.RuntimeException


object Rule {

    // over Blocker rule mode, both IFW and PM are applied
    fun export(context: Context, packageName: String): RulesResult {
        val pm = context.packageManager
        val applicationInfo = ApplicationUtil.getApplicationComponents(pm, packageName)
        val rule = BlockerRule(packageName = applicationInfo.packageName, versionName = applicationInfo.versionName, versionCode = applicationInfo.longVersionCode.toInt())
        var disabledComponentsCount = 0
        val ifwController = IntentFirewallImpl.getInstance(context, packageName)
        applicationInfo.receivers?.forEach {
            if (ifwController.getComponentEnableState(it.packageName, it.name)) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.RECEIVER, EControllerMethod.IFW))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.RECEIVER, EControllerMethod.IFW))
                disabledComponentsCount++
            }
            if (ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.RECEIVER, EControllerMethod.PM))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.RECEIVER, EControllerMethod.PM))
                disabledComponentsCount++
            }
        }
        applicationInfo.services?.forEach {
            if (ifwController.getComponentEnableState(it.packageName, it.name)) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.SERVICE, EControllerMethod.IFW))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.SERVICE, EControllerMethod.IFW))
                disabledComponentsCount++
            }
            if (ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.SERVICE, EControllerMethod.PM))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.SERVICE, EControllerMethod.PM))
                disabledComponentsCount++
            }
        }
        applicationInfo.activities?.forEach {
            if (ifwController.getComponentEnableState(it.packageName, it.name)) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.ACTIVITY, EControllerMethod.IFW))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.ACTIVITY, EControllerMethod.IFW))
                disabledComponentsCount++
            }
            if (ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.ACTIVITY, EControllerMethod.PM))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.ACTIVITY, EControllerMethod.PM))
                disabledComponentsCount++
            }
        }
        applicationInfo.providers?.forEach {
            if (ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, true, EComponentType.RECEIVER, EControllerMethod.PM))
            } else {
                rule.components.add(ComponentRule(it.packageName, it.name, false, EComponentType.RECEIVER, EControllerMethod.PM))
                disabledComponentsCount++
            }
        }
        return if (rule.components.isNotEmpty()) {
            val ruleFile = File(getBlockerRuleFolder(context), packageName + ConstantUtil.EXTENSION_JSON)
            saveRuleToStorage(rule, ruleFile)
            RulesResult(true, disabledComponentsCount, 0)
        } else {
            RulesResult(false, 0, 0)
        }
    }

    // over Blocker rule mode, both IFW and PM are applied
    fun import(context: Context, file: File): RulesResult {
        val jsonReader = JsonReader(FileReader(file))
        val appRule = Gson().fromJson<BlockerRule>(jsonReader, BlockerRule::class.java)
                ?: return RulesResult(false, 0, 0)
        var succeedCount = 0
        var failedCount = 0
        // val total = appRule.components.size
        val controller = getController(context)
        var ifwController: IntentFirewall? = null
        // Detects if contains IFW rules, if exists, create a new controller.
        appRule.components.forEach ifwDetection@{
            if (it.method == EControllerMethod.IFW) {
                ifwController = IntentFirewallImpl.getInstance(context, appRule.packageName)
                return@ifwDetection
            }
        }
        try {
            appRule.components.forEach {
                val controllerResult = when (it.method) {
                    EControllerMethod.IFW -> {
                        when (it.type) {
                            EComponentType.RECEIVER -> {
                                if (it.enabled) {
                                    ifwController?.add(it.packageName, it.name, ComponentType.BROADCAST) ?: false
                                } else {
                                    ifwController?.remove(it.packageName, it.name, ComponentType.BROADCAST) ?: false
                                }
                            }
                            EComponentType.SERVICE -> {
                                if (it.enabled) {
                                    ifwController?.add(it.packageName, it.name, ComponentType.SERVICE) ?: false
                                } else {
                                    ifwController?.remove(it.packageName, it.name, ComponentType.SERVICE) ?: false
                                }
                            }
                            EComponentType.ACTIVITY -> {
                                if (it.enabled) {
                                    ifwController?.add(it.packageName, it.name, ComponentType.ACTIVITY) ?: false
                                } else {
                                    ifwController?.remove(it.packageName, it.name, ComponentType.ACTIVITY) ?: false
                                }
                            }
                            // content provider needs PM to implement it
                            EComponentType.PROVIDER -> {
                                if (it.enabled) {
                                    controller.enable(it.packageName, it.name)
                                } else {
                                    controller.disable(it.packageName, it.name)
                                }
                            }
                            EComponentType.UNKNOWN -> false
                        }
                    }
                    EControllerMethod.PM -> {
                        if (it.enabled) {
                            controller.enable(it.packageName, it.name)
                        } else {
                            controller.disable(it.packageName, it.name)
                        }
                    }
                }
                if (controllerResult) {
                    succeedCount++
                } else {
                    failedCount++
                }
            }
            ifwController?.save()
        } catch (e: Exception) {
            e.printStackTrace()
            return RulesResult(false, succeedCount, failedCount)
        }
        return RulesResult(true, succeedCount, failedCount)
    }

    fun importMatRules(context: Context, file: File, action: (context: Context, name: String, current: Int, total: Int) -> Unit): RulesResult {
        var succeedCount = 0
        var failedCount = 0
        val total = countLines(file)
        val controller = getController(context)
        val uninstalledAppList = mutableListOf<String>()
        try {
            file.forEachLine {
                if (it.trim().isEmpty() || !it.contains("/")) {
                    failedCount++
                    return@forEachLine
                }
                val splitResult = it.split("/")
                if (splitResult.size != 2) {
                    failedCount++
                    return@forEachLine
                }
                val packageName = splitResult[0]
                val name = splitResult[1]
                if (isApplicationUninstalled(context, uninstalledAppList, packageName)) {
                    failedCount++
                    return@forEachLine
                }
                val result = controller.disable(packageName, name)
                if (result) {
                    succeedCount++
                } else {
                    failedCount++
                }
                action(context, name, (succeedCount + failedCount), total)
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
        FileUtils.listFiles(ifwBackupFolder)
                .filter {
                    // remove the Greenify cut-off configs
                    it.split(File.separator).last() == "gib.xml" ||
                            it.split(File.separator).last() == "grx.xml"
                }.forEach {
                    File(it).delete()
                }
        return FileUtils.count(ifwBackupFolder)
    }

    // over Ifw rule mode, only three components are exported
    fun importIfwRules(context: Context): Int {
        val controller = ComponentControllerProxy.getInstance(EControllerMethod.IFW, context)
        // var succeedCount = 0
        val ifwBackupFolder = getBlockerIFWFolder(context)
        FileUtils.listFiles(ifwBackupFolder).filter {
            it.endsWith("xml")
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
            // succeedCount++
        }
        // return succeedCount
        return FileUtils.count(ifwBackupFolder)
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
            return false
        }
        return result
    }

    @Throws(RuntimeException::class, IOException::class)
    fun exportPrescription(context: Context,
                           packageName: String, className: String,
                           typeC: String, sender: String,
                           action: String?, cat: String?,
                           typeF: String?, scheme: String?,
                           auth: String?, path: String?, pathOption: String?): Int {
        val prescriptionFolder = getBlockerPrescriptionFolder(context)
        val filename = packageName.split(".").last() + "." + className.split(".").last() + ConstantUtil.EXTENSION_XML
        val content = PrescriptionUtil.template(packageName, className, typeC, sender, action, cat, typeF, scheme, auth, path, pathOption)
        FileWriter(File(prescriptionFolder, filename)).apply {
            write(PrescriptionUtil.head())
            write(PrescriptionUtil.header())
            write(content)
            write(PrescriptionUtil.footer())
            close()
        }
        return 1
    }

    private fun countLines(file: File): Int {
        var lines = 0
        if (!file.exists()) {
            return lines
        }
        file.forEachLine {
            if (it.trim().isEmpty()) {
                return@forEachLine
            }
            lines++
        }
        return lines
    }

    private fun saveRuleToStorage(rule: BlockerRule, dest: File) {
        if (dest.exists()) {
            dest.delete()
        }
        dest.writeText(GsonBuilder().setPrettyPrinting().create().toJson(rule))
    }

    private fun isApplicationUninstalled(context: Context, savedList: MutableList<String>, packageName: String): Boolean {
        if (packageName.trim().isEmpty()) {
            return true
        }
        if (savedList.contains(packageName)) {
            return true
        }
        if (!ApplicationUtil.isAppInstalled(context.packageManager, packageName)) {
            savedList.add(packageName)
            return true
        }
        return false
    }

    fun getBlockerRuleFolder(context: Context): String {
        val path = FileUtils.getExternalStoragePath(context) +
                File.separator + "rule" + File.separator
        if (!File(path).exists()) {
            File(path).mkdirs()
        }
        return path
    }

    private fun getBlockerIFWFolder(context: Context): String {
        val path = FileUtils.getExternalStoragePath(context) +
                File.separator + "ifw" + File.separator
        if (!File(path).exists()) {
            File(path).mkdirs()
        }
        return path
    }

    private fun getBlockerPrescriptionFolder(context: Context): String {
        val path = FileUtils.getExternalStoragePath(context) +
                File.separator + "prescription" + File.separator
        if (!File(path).exists()) {
            File(path).mkdirs()
        }
        return path
    }

    private fun getController(context: Context): IController {
        val controllerType = PreferenceUtil.getControllerType(context)
        return ComponentControllerProxy.getInstance(controllerType, context)
    }
}
