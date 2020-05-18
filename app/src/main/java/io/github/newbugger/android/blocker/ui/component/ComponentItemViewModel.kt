package io.github.newbugger.android.blocker.ui.component

data class ComponentItemViewModel(
        var state: Boolean = true,
        var ifwState: Boolean = true,
        var name: String = "",
        var simpleName: String = "",
        var packageName: String = "",
        var bestComment: String = "",
        var isRunning: Boolean = false
)