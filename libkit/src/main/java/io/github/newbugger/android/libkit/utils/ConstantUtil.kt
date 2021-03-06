package io.github.newbugger.android.libkit.utils


object ConstantUtil {

    const val APPLICATION = "application"
    const val CATEGORY = "category"
    const val PACKAGE_NAME = "package_name"
    const val STATUS_BAR_ALPHA = 32

    // .libkit.utils.ApplicationUtil
    const val BLOCKER_PACKAGE_NAME = "io.github.newbugger.android.blocker"
    const val BLOCKED_CONF_NAME = "Blocked"
    const val BLOCKED_APP_LIST_KEY = "key_blocked_app_list"

    // .ui.home.ApplicationListFragment
    const val IS_SYSTEM: String = "IS_SYSTEM"

    // .ui.settings
    const val documentRequestCode = 1

    // .core.root.RootController
    const val COMMAND_ENABLE_COMPONENT = "pm enable %s/%s"
    const val COMMAND_DISABLE_COMPONENT = "pm disable %s/%s"

    // .rule.Rule
    const val EXTENSION_JSON = ".json"
    const val EXTENSION_XML = ".xml"
    const val NAME_RULE_BLOCKER = "rule"
    const val NAME_RULE_IFW = "ifw"
    const val NAME_RULE_PRESCRIPTION = "prescription"
    const val NAME_RULE_TEST = "test"
    const val NAME_RULE_TEST_TITLE = "filename.txt"
    const val NAME_RULE_TEST_CONTENT = "test"
    const val NAME_APP_NAME_DEFAULT = "Blocker"
    const val NAME_APP_NAME_RULE = NAME_RULE_BLOCKER
    const val NAME_APP_NAME_IFW = NAME_RULE_IFW
    const val NAME_APP_NAME_PRESCRIPTION = NAME_RULE_PRESCRIPTION

}
