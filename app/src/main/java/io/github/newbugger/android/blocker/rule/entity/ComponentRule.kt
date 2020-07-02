package io.github.newbugger.android.blocker.rule.entity

import io.github.newbugger.android.blocker.core.root.EControllerMethod
import io.github.newbugger.android.blocker.ui.component.EComponentType

data class ComponentRule(
        var packageName: String = "",
        var name: String = "",
        var enabled: Boolean = true,
        var type: EComponentType = EComponentType.UNKNOWN,
        var method: EControllerMethod = EControllerMethod.PM
)
