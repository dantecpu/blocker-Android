package io.github.newbugger.android.libkit.utils


object ConstantUtil {

    const val APPLICATION = "application"
    const val CATEGORY = "category"
    const val PACKAGE_NAME = "package_name"
    const val STATUS_BAR_ALPHA = 32

    // .ui.settings.PreferenceFragment
    const val matRulePathRequestCode = 100
    const val aboutURL = "https://github.com/NewBugger/blocker-Android"

    // .libkit.utils.ApplicationUtil
    const val BLOCKER_PACKAGE_NAME = "io.github.newbugger.android.blocker"
    const val BLOCKED_CONF_NAME = "Blocked"
    const val BLOCKED_APP_LIST_KEY = "key_blocked_app_list"

    // .ui.home.ApplicationListFragment
    const val IS_SYSTEM: String = "IS_SYSTEM"

    // .ui.component.ComponentActivity
    const val sdkUNKNOWN = "Unknown"

    // .core.root.RootController
    const val COMMAND_ENABLE_COMPONENT = "pm enable %s/%s"
    const val COMMAND_DISABLE_COMPONENT = "pm disable %s/%s"

    // .rule.Rule
    const val EXTENSION_JSON = ".json"
    const val EXTENSION_XML = ".xml"

}
