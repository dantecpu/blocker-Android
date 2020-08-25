package io.github.newbugger.android.blocker.ui.component

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.core.ComponentControllerProxy
import io.github.newbugger.android.blocker.core.IController
import io.github.newbugger.android.blocker.core.root.EControllerMethod
import io.github.newbugger.android.blocker.rule.Rule
import io.github.newbugger.android.blocker.rule.entity.RulesResult
import io.github.newbugger.android.blocker.util.DialogUtil
import io.github.newbugger.android.blocker.util.MediaStoreLocalUtil
import io.github.newbugger.android.blocker.util.PreferenceUtil
import io.github.newbugger.android.blocker.util.ToastUtil
import io.github.newbugger.android.libkit.entity.getSimpleName
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.libkit.utils.ServiceHelper
import kotlinx.coroutines.*
import java.io.File


class ComponentPresenter(val context: Context, var view: ComponentContract.View?, val packageName: String) : ComponentContract.Presenter, IController {
    override var currentComparator: EComponentComparatorType = EComponentComparatorType.NAME_ASCENDING  // changed order
    private val controllerType = PreferenceUtil.getControllerType(context)
    private val pm: PackageManager
    private val serviceHelper by lazy { ServiceHelper(packageName) }
    private val ifwController by lazy { ComponentControllerProxy.getInstance(EControllerMethod.IFW, context) }
    private val controller: IController by lazy {
        controllerType
        ComponentControllerProxy.getInstance(controllerType, context)
    }
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        DialogUtil().showWarningDialogWithMessage(context, throwable)
        throwable.printStackTrace()
    }
    private lateinit var type: EComponentType

    init {
        view?.presenter = this
        pm = context.packageManager
    }

    override fun start(context: Context) {

    }

    override fun destroy() {
        view = null
    }

    override fun loadComponents(packageName: String, type: EComponentType) {
        view?.setLoadingIndicator(true)
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            if (type == EComponentType.SERVICE) {
                serviceHelper.refreshRoot()
            }
            val componentList = getComponents(packageName, type)
            val viewModels = initViewModel(componentList)
            val sortedViewModels = sortComponentList(viewModels, currentComparator)
            launch(Dispatchers.Main) {
                view?.setLoadingIndicator(false)
                if (sortedViewModels.isEmpty()) {
                    view?.showNoComponent()
                } else {
                    view?.showComponentList(sortedViewModels.toMutableList())
                }
            }
        }
    }

    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        return controller.switchComponent(packageName, componentName, state)
    }

    override fun enable(packageName: String, componentName: String): Boolean {
        val handler = CoroutineExceptionHandler { _, throwable ->
            DialogUtil().showWarningDialogWithMessage(context, throwable)
            view?.refreshComponentState(componentName)
        }
        CoroutineScope(Dispatchers.Default + handler).launch {
            when (controllerType) {
                EControllerMethod.IFW -> {
                    if (!checkIFWState(packageName, componentName)) {
                        ComponentControllerProxy.getInstance(EControllerMethod.IFW, context).enable(packageName, componentName)
                    }
                }
                EControllerMethod.PM -> {
                    if (!ApplicationUtil.checkComponentIsEnabled(context.packageManager, ComponentName(packageName, componentName))) {
                        ComponentControllerProxy.getInstance(EControllerMethod.PM, context).enable(packageName, componentName)
                    }
                }
            }
            controller.enable(packageName, componentName)
            launch(Dispatchers.Main) {
                view?.refreshComponentState(componentName)
            }
        }
        return true
    }

    override fun disable(packageName: String, componentName: String): Boolean {
        val handler = CoroutineExceptionHandler { _, throwable ->
            DialogUtil().showWarningDialogWithMessage(context, throwable)
            view?.refreshComponentState(componentName)
        }
        CoroutineScope(Dispatchers.Default + handler).launch {
            controller.disable(packageName, componentName)
            launch(Dispatchers.Main) {
                view?.refreshComponentState(componentName)
            }
        }
        return true
    }

    override fun sortComponentList(components: List<ComponentItemViewModel>, type: EComponentComparatorType):
            List<ComponentItemViewModel> {
        val sortedComponents = when (type) {
            EComponentComparatorType.SIMPLE_NAME_ASCENDING -> components.sortedBy { it.simpleName }
            EComponentComparatorType.SIMPLE_NAME_DESCENDING -> components.sortedByDescending { it.simpleName }
            EComponentComparatorType.NAME_ASCENDING -> components.sortedBy { it.name }
            EComponentComparatorType.NAME_DESCENDING -> components.sortedByDescending { it.name }
        }
        return sortedComponents.sortedWith(compareBy({ !it.isRunning }, { !it.state }, { !it.ifwState }))
    }

    override fun addToIFW(packageName: String, componentName: String, type: EComponentType) {
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            ifwController.disable(packageName, componentName)
            launch(Dispatchers.Main) {
                view?.refreshComponentState(componentName)
            }
        }
    }

    override fun removeFromIFW(packageName: String, componentName: String, type: EComponentType) {
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            ifwController.enable(packageName, componentName)
            launch(Dispatchers.Main) {
                view?.refreshComponentState(componentName)
            }
        }
    }

    // TODO: specify details for Prescription
    override fun generatePrescription(context: Context, packageName: String, componentName: String, typeC: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            Rule.exportPrescription(context, packageName, componentName, typeC, "other",
                    null, null, null, null, null, null, null)
            launch(Dispatchers.Main) {
                view?.refreshComponentState(componentName)
            }
        }
    }

    override fun checkComponentEnableState(packageName: String, componentName: String): Boolean {
        return ApplicationUtil.checkComponentIsEnabled(pm, ComponentName(packageName, componentName))
    }

    override fun batchEnable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        TODO("Won't implemented")
    }

    override fun batchDisable(componentList: List<ComponentInfo>, action: (info: ComponentInfo) -> Unit): Int {
        TODO("Won't  implemented")
    }

    override fun checkIFWState(packageName: String, componentName: String): Boolean {
        return ifwController.checkComponentEnableState(packageName, componentName)
    }

    override fun getComponentViewModel(packageName: String, componentName: String): ComponentItemViewModel {
        val viewModel = ComponentItemViewModel(packageName = packageName, name = componentName)
        viewModel.simpleName = componentName.split(".").last()
        updateComponentViewModel(viewModel)
        return viewModel
    }

    override fun updateComponentViewModel(viewModel: ComponentItemViewModel) {
        viewModel.state = ApplicationUtil.checkComponentIsEnabled(pm,
                ComponentName(viewModel.packageName, viewModel.name))
        if (controllerType == EControllerMethod.IFW) {
            viewModel.ifwState = ifwController.checkComponentEnableState(viewModel.packageName, viewModel.name)
        }
        if (type == EComponentType.SERVICE) {
            viewModel.isRunning = isServiceRunning(viewModel.name)
        }
    }

    override fun disableAllComponents(packageName: String, type: EComponentType) {
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            val components = getComponents(packageName, type)
            controller.batchDisable(components) { componentInfo ->
                launch(Dispatchers.Main) {
                    view?.refreshComponentState(componentInfo.name)
                }
            }
            launch(Dispatchers.Main) {
                view?.showActionDone()
                loadComponents(packageName, type)
            }
        }
    }

    override fun enableAllComponents(packageName: String, type: EComponentType) {
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            val components = getComponents(packageName, type)
            ifwController.batchEnable(components) { componentInfo ->
                if (!ApplicationUtil.checkComponentIsEnabled(context.packageManager,
                                ComponentName(componentInfo.packageName, componentInfo.name))) {
                    ComponentControllerProxy.getInstance(EControllerMethod.PM, context)
                            .enable(componentInfo.packageName, componentInfo.name)
                }
                launch(Dispatchers.Main) {
                    view?.refreshComponentState(componentInfo.name)
                }
            }
            launch(Dispatchers.Main) {
                view?.showActionDone()
                loadComponents(packageName, type)
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun exportRule(packageName: String) {
        exportBlockerRule(packageName)
    }

    private fun exportBlockerRule(packageName: String) {
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            val result = Rule.export(context, packageName)
            launch(Dispatchers.Main) {
                if (result.isSucceed) {
                    view?.showActionDone()
                } else {
                    view?.showActionFail()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun importRule(packageName: String) {
        importBlockerRule(packageName)
    }

    override fun isServiceRunning(componentName: String): Boolean {
        return serviceHelper.isServiceRunning(componentName)
    }

    @SuppressLint("CheckResult")
    private fun importBlockerRule(packageName: String) {
        view?.showToastMessage(context.getString(R.string.processing), Toast.LENGTH_SHORT)
        CoroutineScope(Dispatchers.Default + exceptionHandler).launch {
            val destFile = if (Build.VERSION.SDK_INT >= 29) {
                MediaStoreLocalUtil.readText(context, Rule.NAME_RULE_BLOCKER, packageName + ConstantUtil.EXTENSION_JSON)
            } else {
                val blockerFolder = Rule.getBlockerRuleFolder(context)
                File(blockerFolder, packageName + ConstantUtil.EXTENSION_JSON).readText()
            }
            val result = if (destFile?.isNotEmpty() == true) {
                Rule.import(context, destFile)
            } else {
                RulesResult(false, 0, 0)
            }
            launch(Dispatchers.Main) {
                if (result.isSucceed) {
                    ToastUtil.showToast(context.getString(R.string.done))
                } else {
                    ToastUtil.showToast(context.getString(R.string.import_fail_message))
                }
            }
        }
    }

    private fun initViewModel(componentList: List<ComponentInfo>): List<ComponentItemViewModel> {
        val viewModels = ArrayList<ComponentItemViewModel>()
        componentList.forEach {
            viewModels.add(getComponentViewModel(it.packageName, it.name))
        }
        return viewModels
    }

    private fun getComponents(packageName: String, type: EComponentType): MutableList<out ComponentInfo> {
        this.type = type
        val components = when (type) {
            EComponentType.RECEIVER -> ApplicationUtil.getReceiverList(pm, packageName)
            EComponentType.ACTIVITY -> ApplicationUtil.getActivityList(pm, packageName)
            EComponentType.SERVICE -> ApplicationUtil.getServiceList(pm, packageName)
            EComponentType.PROVIDER -> ApplicationUtil.getProviderList(pm, packageName)
            else -> ArrayList<ComponentInfo>()
        }
        return components.asSequence().sortedBy { it.getSimpleName() }.toMutableList()
    }
}