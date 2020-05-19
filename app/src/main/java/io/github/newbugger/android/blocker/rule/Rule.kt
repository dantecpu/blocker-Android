package io.github.newbugger.android.blocker.rule

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import com.elvishew.xlog.XLog
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
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.FileUtils
import io.github.newbugger.android.libkit.utils.StorageUtils
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object Rule {
    const val EXTENSION = ".json"
    private val logger = XLog.tag("Rule").build()

    // TODO remove template code
    fun export(context: Context, packageName: String): RulesResult {
        logger.i("Backup rules for $packageName")
        val pm = context.packageManager
        val applicationInfo = ApplicationUtil.getApplicationComponents(pm, packageName)
        val rule = BlockerRule(packageName = applicationInfo.packageName, versionName = applicationInfo.versionName, versionCode = 1)
        var disabledComponentsCount = 0
        val ifwController = IntentFirewallImpl.getInstance(context, packageName)
        applicationInfo.receivers?.forEach {
            if (!ifwController.getComponentEnableState(it.packageName, it.name)) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.RECEIVER, EControllerMethod.IFW))
                disabledComponentsCount++
            }
            if (!ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.RECEIVER))
                disabledComponentsCount++
            }
        }
        applicationInfo.services?.forEach {
            if (!ifwController.getComponentEnableState(it.packageName, it.name)) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.SERVICE, EControllerMethod.IFW))
                disabledComponentsCount++
            }
            if (!ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.SERVICE))
                disabledComponentsCount++
            }
        }
        applicationInfo.activities?.forEach {
            if (!ifwController.getComponentEnableState(it.packageName, it.name)) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.ACTIVITY, EControllerMethod.IFW))
                disabledComponentsCount++
            }
            if (!ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.ACTIVITY))
                disabledComponentsCount++
            }
        }
        applicationInfo.providers?.forEach {
            if (!ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(it.packageName, it.name))) {
                rule.components.add(ComponentRule(it.packageName, it.name, EComponentType.RECEIVER))
                disabledComponentsCount++
            }
        }
        return if (rule.components.isNotEmpty()) {
            val ruleFile = File(getBlockerRuleFolder(context), packageName + EXTENSION)
            saveRuleToStorage(rule, ruleFile)
            RulesResult(true, disabledComponentsCount, 0)
        } else {
            RulesResult(false, 0, 0)
        }
    }

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
                            EComponentType.RECEIVER -> ifwController?.add(it.packageName, it.name, ComponentType.BROADCAST)
                                    ?: false
                            EComponentType.SERVICE -> ifwController?.add(it.packageName, it.name, ComponentType.SERVICE)
                                    ?: false
                            EComponentType.ACTIVITY -> ifwController?.add(it.packageName, it.name, ComponentType.ACTIVITY)
                                    ?: false
                            else -> controller.disable(it.packageName, it.name)
                        }
                    }
                    else -> controller.disable(it.packageName, it.name)
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
            logger.e(e.message)
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
                    logger.d("Failed to change component state for : $it")
                    failedCount++
                }
                action(context, name, (succeedCount + failedCount), total)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.e(e.message)
            return RulesResult(false, succeedCount, failedCount)
        }
        return RulesResult(true, succeedCount, failedCount)
    }

    fun exportIfwRules(context: Context): Int {
        val ifwFolder = StorageUtils.getIfwFolder()
        val ifwBackupFolder = getBlockerIFWFolder(context)
        if (!ifwBackupFolder.exists()) {
            ifwBackupFolder.mkdirs()
        }
        val files = FileUtils.listFiles(ifwFolder)
        files.forEach {
            val filename = it.split(File.separator).last()
            val content = FileUtils.read(it)
            val file = File(getBlockerIFWFolder(context), filename)
            val fileWriter = FileWriter(file)
            fileWriter.write(content)
            fileWriter.close()
        }
        return files.count()
    }

    fun importIfwRules(context: Context): Int {
        val ifwBackupFolder = getBlockerIFWFolder(context)
        val controller = ComponentControllerProxy.getInstance(EControllerMethod.IFW, context)
        var succeedCount = 0
        ifwBackupFolder.listFiles { file -> file.isFile && file.name.endsWith(".xml") }
                .forEach {
                    val rule = RuleSerializer.deserialize(it) ?: return@forEach
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
            logger.e(e.message)
            return false
        }
        return result
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

    fun getBlockerRuleFolder(context: Context): File {
        val path = File(FileUtils.getExternalStoragePath(context).let { it + "rule" })
        if (!path.exists()) {
            path.mkdirs()
        }
        return path
    }

    private fun getBlockerIFWFolder(context: Context): File {
        val path = File(FileUtils.getExternalStoragePath(context).let { it + "ifw" })
        if (!path.exists()) {
            path.mkdirs()
        }
        return path
    }

    private fun getController(context: Context): IController {
        val controllerType = PreferenceUtil.getControllerType(context)
        return ComponentControllerProxy.getInstance(controllerType, context)
    }
}
