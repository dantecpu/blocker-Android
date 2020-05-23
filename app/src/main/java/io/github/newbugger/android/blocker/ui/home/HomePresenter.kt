package io.github.newbugger.android.blocker.ui.home

import android.content.Context
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.core.shizuku.ShizukuApi
import io.github.newbugger.android.blocker.util.DialogUtil
import io.github.newbugger.android.blocker.util.PreferenceUtil
import io.github.newbugger.android.libkit.entity.Application
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.ManagerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class HomePresenter(private var homeView: HomeContract.View?) : HomeContract.Presenter {
    private var context: Context? = null
    private val tag = "io.github.newbugger.android.blocker.ui.home.HomePresenter"
    private val exceptionHandler = { e: Throwable ->
        GlobalScope.launch(Dispatchers.Main) {
            DialogUtil().showWarningDialogWithMessage(context, e)
        }
        e.printStackTrace()
    }

    override fun start(context: Context) {
        this.context = context
        homeView?.presenter = this
    }

    override fun destroy() {
        context = null
        homeView = null
    }

    override fun loadApplicationList(context: Context, isSystemApplication: Boolean) {
        homeView?.setLoadingIndicator(true)
        doAsync(exceptionHandler) {
            val applications: MutableList<Application> = when (isSystemApplication) {
                false -> ApplicationUtil.getThirdPartyApplicationList(context)
                true -> ApplicationUtil.getSystemApplicationList(context)
            }
            val sortedList = sortApplicationList(applications)
            uiThread {
                if (sortedList.isEmpty()) {
                    homeView?.showNoApplication()
                } else {
                    homeView?.showApplicationList(sortedList)
                }
                homeView?.setLoadingIndicator(false)
            }
        }
    }

    override fun openApplicationDetails(application: Application) {
        homeView?.showApplicationDetailsUi(application)
    }

    override fun sortApplicationList(applications: List<Application>): MutableList<Application> {
        val sortedList = when (currentComparator) {
            ApplicationComparatorType.ASCENDING_BY_LABEL -> applications.asSequence().sortedBy { it.label }
            ApplicationComparatorType.DESCENDING_BY_LABEL -> applications.asSequence().sortedByDescending { it.label }
            ApplicationComparatorType.INSTALLATION_TIME -> applications.asSequence().sortedByDescending { it.firstInstallTime }
            ApplicationComparatorType.LAST_UPDATE_TIME -> applications.asSequence().sortedByDescending { it.lastUpdateTime }
        }
        return sortedList.asSequence().sortedWith(compareBy({ !it.isBlocked }, { !it.isEnabled })).toMutableList()
    }

    override fun forceStop(packageName: String) {
        doAsync(exceptionHandler) {
            ManagerUtils.forceStop(packageName)
        }
    }

    // too simple implement: long press at app list then click Disable menu
    // indeed do not need component presenter
    override fun enableApplication(packageName: String) {
        doAsync(exceptionHandler) {
            if (!PreferenceUtil.checkShizukuType(context!!))
                ManagerUtils.enableApplication(packageName)
            else
                ShizukuApi.setComponentRemote(packageName, null, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            uiThread {
                homeView?.updateState(packageName)
            }
        }
    }

    override fun disableApplication(packageName: String) {
        doAsync(exceptionHandler) {
            if (!PreferenceUtil.checkShizukuType(context!!))
                ManagerUtils.disableApplication(packageName)
            else
                ShizukuApi.setComponentRemote(packageName, null, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
            uiThread {
                homeView?.updateState(packageName)
            }
        }
    }

    // TODO: what is blockApplication ?
    override fun blockApplication(packageName: String) {
        doAsync(exceptionHandler) {
            ApplicationUtil.addBlockedApplication(context!!, packageName)
            uiThread {
                homeView?.updateState(packageName)
            }
        }
    }

    override fun unblockApplication(packageName: String) {
        doAsync(exceptionHandler) {
            ApplicationUtil.removeBlockedApplication(context!!, packageName)
            uiThread {
                homeView?.updateState(packageName)
            }
        }
    }

    override var currentComparator = ApplicationComparatorType.DESCENDING_BY_LABEL
        set(comparator) {
            field = comparator
            context?.let {
                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                editor.putInt(it.getString(R.string.key_pref_comparator_type), comparator.value)
                editor.apply()
            }
        }
        get() {
            context?.let {
                val pref = PreferenceManager.getDefaultSharedPreferences(context)
                val comparatorType = pref.getInt(it.getString(R.string.key_pref_comparator_type), 0)
                return ApplicationComparatorType.from(comparatorType)
            }
            return ApplicationComparatorType.DESCENDING_BY_LABEL
        }
}
