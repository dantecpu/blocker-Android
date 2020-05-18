package io.github.newbugger.android.blocker.ui.settings

import androidx.annotation.StringRes
import io.github.newbugger.android.blocker.base.BasePresenter
import io.github.newbugger.android.blocker.base.BaseView
import kotlinx.coroutines.Job

interface SettingsContract : BaseView<SettingsContract.SettingsPresenter> {
    interface SettingsView {
        fun showExportResult(isSucceed: Boolean, successfulCount: Int, failedCount: Int)
        fun showImportResult(isSucceed: Boolean, successfulCount: Int, failedCount: Int)
        fun showResetResult(isSucceed: Boolean)
        fun showMessage(@StringRes res: Int)
        fun showDialog(title: String, message: String, action: () -> Unit)
        fun showDialog(title: String, message: String, file: String?, action: (file: String?) -> Unit)
    }

    interface SettingsPresenter : BasePresenter {
        fun exportAllRules(): Job
        fun importAllRules(): Job
        fun exportAllIfwRules(): Job
        fun importAllIfwRules(): Job
        fun importMatRules(filePath: String?)
        fun resetIFW()
    }
}