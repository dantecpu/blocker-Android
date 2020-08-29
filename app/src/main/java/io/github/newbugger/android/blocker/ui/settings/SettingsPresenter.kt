package io.github.newbugger.android.blocker.ui.settings

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.rule.Rule
import io.github.newbugger.android.blocker.util.storage.ModernStorageLocalUtil
import io.github.newbugger.android.blocker.util.NotificationUtil
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.libkit.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException


class SettingsPresenter(
    private val context: Context,
    private val settingsView: SettingsContract.SettingsView
) : SettingsContract.SettingsPresenter {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val exceptionHandler = CoroutineExceptionHandler { _, e: Throwable ->
        e.printStackTrace()
        NotificationUtil.cancelNotification(context)
    }
    private val uiScope = CoroutineScope(Dispatchers.Main + exceptionHandler)

    @Throws(RuntimeException::class, IOException::class)
    override fun exportAllRules() = uiScope.launch {
        var succeedCount = 0
        var failedCount = 0
        var appCount: Int
        val errorHandler = CoroutineExceptionHandler { _, e ->
            failedCount++
            e.printStackTrace()
        }
        withContext(Dispatchers.IO + errorHandler) {
            val applicationList = ApplicationUtil.getThirdPartyApplicationList(context)
            val applicationListGoogle = ApplicationUtil.getGoogleSystemApplicationList(context)
            appCount = applicationList.size + applicationListGoogle.size
            notificationBuilder = NotificationUtil.createProcessingNotification(context, appCount)
            applicationList.forEach { currentApp ->
                Rule.export(context, currentApp.packageName)
                succeedCount++
                NotificationUtil.updateProcessingNotification(
                    context,
                    currentApp.label,
                    (succeedCount + failedCount),
                    appCount,
                    notificationBuilder
                )
            }
            applicationListGoogle.forEach { currentApp ->
                Rule.export(context, currentApp.packageName)
                succeedCount++
                NotificationUtil.updateProcessingNotification(
                        context,
                        currentApp.label,
                        (succeedCount + failedCount),
                        appCount,
                        notificationBuilder
                )
            }
            delay(1000L)
            NotificationUtil.finishProcessingNotification(context, succeedCount, notificationBuilder)
        }
        settingsView.showExportResult(true, succeedCount, failedCount)
    }

    @Throws(RuntimeException::class, IOException::class)
    override fun importAllRules() = uiScope.launch {
        var restoredCount = 0
        var rulesCount: Int
        withContext(Dispatchers.IO) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ModernStorageLocalUtil.check(context)) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ModernStorageLocalUtil.readAllText(context, ConstantUtil.NAME_RULE_BLOCKER).also {
                    rulesCount = it.size
                    notificationBuilder = NotificationUtil.createProcessingNotification(context, rulesCount)
                }.forEach { (packageName, text) ->
                    if (packageName.isNullOrEmpty() || text.isNullOrEmpty()) return@forEach
                    if (!ApplicationUtil.isAppInstalled(context.packageManager, packageName)) {
                        return@forEach
                    }
                    Rule.import(context, text)
                    restoredCount++
                    NotificationUtil.updateProcessingNotification(
                            context,
                            packageName,
                            restoredCount,
                            rulesCount,
                            notificationBuilder
                    )
                }
            } else {
                rulesCount = FileUtils.getFileCounts(Rule.getBlockerRuleFolder(context), ConstantUtil.EXTENSION_JSON)
                notificationBuilder = NotificationUtil.createProcessingNotification(context, rulesCount)
                FileUtils.listFiles(Rule.getBlockerRuleFolder(context)).filter {
                    it.endsWith(ConstantUtil.EXTENSION_JSON)
                }.forEach {
                    val packageName = it.replace(ConstantUtil.EXTENSION_JSON, "")
                    if (!ApplicationUtil.isAppInstalled(context.packageManager, packageName)) {
                        return@forEach
                    }
                    Rule.import(context, File(it).readText())
                    restoredCount++
                    NotificationUtil.updateProcessingNotification(
                            context,
                            packageName,
                            restoredCount,
                            rulesCount,
                            notificationBuilder
                    )
                }
            }
        }
        NotificationUtil.finishProcessingNotification(context, restoredCount, notificationBuilder)
    }

    @Throws(RuntimeException::class, IOException::class)
    override fun exportAllIfwRules() = uiScope.launch {
        withContext(Dispatchers.IO) {
            notificationBuilder = NotificationUtil.createProcessingNotification(context, 0)
            val exportedCount = Rule.exportIfwRules(context)
            NotificationUtil.finishProcessingNotification(context, exportedCount, notificationBuilder)
        }
    }

    @Throws(RuntimeException::class, IOException::class)
    override fun importAllIfwRules() = uiScope.launch {
        var count: Int
        withContext(Dispatchers.IO) {
            notificationBuilder = NotificationUtil.createProcessingNotification(context, 0)
            count = Rule.importIfwRules(context)
            NotificationUtil.finishProcessingNotification(context, count, notificationBuilder)
        }
        settingsView.showExportResult(true, count, 0)
    }

    @Throws(RuntimeException::class, IOException::class)
    override fun resetIFW() {
        val errorHandler = CoroutineExceptionHandler { _, e ->
            e.printStackTrace()
            settingsView.showMessage(R.string.ifw_reset_error)
        }
        CoroutineScope(Dispatchers.Main + errorHandler).launch {
            var result: Boolean
            withContext(Dispatchers.IO) {
                result = Rule.resetIfw()
            }
            if (result) {
                settingsView.showMessage(R.string.done)
            } else {
                settingsView.showMessage(R.string.ifw_reset_error)
            }
        }
    }

    override fun start(context: Context) {

    }

    override fun destroy() {

    }

}
