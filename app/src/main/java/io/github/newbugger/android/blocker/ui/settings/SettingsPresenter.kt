package io.github.newbugger.android.blocker.ui.settings

import android.content.Context
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.stericson.RootTools.RootTools
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.exception.RootUnavailableException
import io.github.newbugger.android.blocker.rule.Rule
import io.github.newbugger.android.blocker.rule.entity.BlockerRule
import io.github.newbugger.android.blocker.util.NotificationUtil
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.FileUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader


class SettingsPresenter(
    private val context: Context,
    private val settingsView: SettingsContract.SettingsView
) : SettingsContract.SettingsPresenter {

    private val notificationUtil = NotificationUtil()
    private val logger = XLog.tag("SettingsPresenter").build()
    private val exceptionHandler = CoroutineExceptionHandler { _, e: Throwable ->
        logger.e("Caught an exception:", e)
        notificationUtil.cancelNotification(context)
    }
    private val uiScope = CoroutineScope(Dispatchers.Main + exceptionHandler)

    override fun exportAllRules() = uiScope.launch {
        var succeedCount = 0
        var failedCount = 0
        var appCount: Int
        val errorHandler = CoroutineExceptionHandler { _, e ->
            failedCount++
            logger.e(e)
        }
        withContext(Dispatchers.IO + errorHandler) {
            checkRootAccess()
            val applicationList = ApplicationUtil.getApplicationList(context)
            appCount = applicationList.size
            notificationUtil.createProcessingNotification(context, appCount)
            applicationList.forEach { currentApp ->
                Rule.export(context, currentApp.packageName)
                succeedCount++
                notificationUtil.updateProcessingNotification(
                    context,
                    currentApp.label,
                    (succeedCount + failedCount),
                    appCount
                )
            }
            delay(1000L)
            notificationUtil.finishProcessingNotification(context, succeedCount)
        }
        settingsView.showExportResult(true, succeedCount, failedCount)
    }

    override fun importAllRules() = uiScope.launch {
        var restoredCount = 0
        var rulesCount: Int
        withContext(Dispatchers.IO) {
            checkRootAccess()
            rulesCount = FileUtils.getFileCounts(
                Rule.getBlockerRuleFolder(context).absolutePath,
                Rule.EXTENSION
            )
            notificationUtil.createProcessingNotification(context, rulesCount)
            FileUtils.listFiles(Rule.getBlockerRuleFolder(context).absolutePath).filter {
                it.endsWith(Rule.EXTENSION)
            }.forEach {
                val rule = Gson().fromJson(FileReader(it), BlockerRule::class.java)
                if (!ApplicationUtil.isAppInstalled(context.packageManager, rule.packageName)) {
                    return@forEach
                }
                Rule.import(context, File(it))
                restoredCount++
                notificationUtil.updateProcessingNotification(
                    context,
                    rule.packageName ?: "",
                    restoredCount,
                    rulesCount
                )
            }
        }
        notificationUtil.finishProcessingNotification(context, restoredCount)
    }


    override fun exportAllIfwRules() = uiScope.launch {
        withContext(Dispatchers.IO) {
            checkRootAccess()
            notificationUtil.createProcessingNotification(context, 0)
            val exportedCount = Rule.exportIfwRules(context)
            notificationUtil.finishProcessingNotification(context, exportedCount)
        }
    }

    override fun importAllIfwRules() = uiScope.launch {
        var count = 0
        withContext(Dispatchers.IO) {
            checkRootAccess()
            notificationUtil.createProcessingNotification(context, 0)
            count = Rule.importIfwRules(context)
            notificationUtil.finishProcessingNotification(context, count)
        }
        settingsView.showExportResult(true, count, 0)
    }

    override fun resetIFW() {
        val errorHandler = CoroutineExceptionHandler { _, e ->
            logger.e(e)
            settingsView.showMessage(R.string.ifw_reset_error)
        }
        CoroutineScope(Dispatchers.Main + errorHandler).launch {
            var result = false
            withContext(Dispatchers.IO) {
                checkRootAccess()
                result = Rule.resetIfw()
            }
            if (result) {
                settingsView.showMessage(R.string.done)
            } else {
                settingsView.showMessage(R.string.ifw_reset_error)
            }
        }
    }

    override fun importMatRules(filePath: String?) {
        val errorHandler = CoroutineExceptionHandler { _, e ->
            logger.e(e)
            notificationUtil.finishProcessingNotification(context, 0)
        }
        CoroutineScope(Dispatchers.IO + errorHandler).launch {
            checkRootAccess()
            notificationUtil.createProcessingNotification(context, 0)
            if (filePath == null) {
                throw NullPointerException("File path cannot be null")
            }
            val file = File(filePath)
            if (!file.exists()) {
                throw FileNotFoundException("Cannot find MyAndroidTools Rule File: ${file.path}")
            }
            val result = Rule.importMatRules(context, file) { context, name, current, total ->
                notificationUtil.updateProcessingNotification(context, name, current, total)
            }
            delay(1000L)
            notificationUtil.finishProcessingNotification(
                context,
                result.failedCount + result.succeedCount
            )
        }
    }

    override fun start(context: Context) {

    }

    override fun destroy() {

    }

    private fun checkRootAccess() {
        if (!RootTools.isAccessGiven()) {
            throw RootUnavailableException()
        }
    }
}