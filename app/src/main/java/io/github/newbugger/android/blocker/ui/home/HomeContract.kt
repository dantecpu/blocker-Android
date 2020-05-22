package io.github.newbugger.android.blocker.ui.home

import android.content.Context
import androidx.annotation.StringRes
import io.github.newbugger.android.blocker.base.BasePresenter
import io.github.newbugger.android.blocker.base.BaseView
import io.github.newbugger.android.libkit.entity.Application
import io.github.newbugger.android.libkit.entity.ETrimMemoryLevel

/**
 * @author Mercury
 * This interface specifies the contract between the view and the presenter
 */

interface HomeContract {
    interface View : BaseView<Presenter> {
        fun setLoadingIndicator(active: Boolean)
        fun searchForApplication(name: String)
        fun showApplicationList(applications: MutableList<Application>)
        fun showNoApplication()
        fun showFilteringPopUpMenu()
        fun showApplicationDetailsUi(application: Application)
        fun showAlert(@StringRes alertMessage: Int, confirmAction:() -> Unit)
        fun showError(@StringRes errorMessage:Int)
        fun showToastMessage(message: String?, length: Int)
        fun showDataCleared()
        fun showForceStopped()
        fun updateState(packageName: String)
    }

    interface Presenter : BasePresenter {
        var currentComparator: ApplicationComparatorType
        fun loadApplicationList(context: Context, isSystemApplication: Boolean)
        fun openApplicationDetails(application: Application)
        fun sortApplicationList(applications: List<Application>): List<Application>
        fun forceStop(packageName: String)
        fun enableApplication(packageName: String)
        fun disableApplication(packageName: String)
        fun clearData(packageName: String)
        fun trimMemory(packageName: String, level: ETrimMemoryLevel)
        fun blockApplication(packageName: String)
        fun unblockApplication(packageName: String)
    }
}