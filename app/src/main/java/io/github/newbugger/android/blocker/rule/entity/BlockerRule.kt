package io.github.newbugger.android.blocker.rule.entity

data class BlockerRule(
        var packageName: String = "",
        var versionName: String = "",
        var versionCode: Int = 0,
        var components: MutableList<ComponentRule> = ArrayList()
)
