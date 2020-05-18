package io.github.newbugger.android.blocker.rule.entity

data class RulesResult(
        var isSucceed: Boolean = true,
        var succeedCount: Int = 0,
        var failedCount: Int = 0
)