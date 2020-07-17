package io.github.newbugger.android.blocker.core.prescription.entity


data class PrescriptionInfo(
        var componentName: String = "",
        var enabled: Boolean = true,
        var typeC: String = "",
        var sender: String = "",
        var action: String? = null,
        var cat: String? = null,
        var typeF: String? = null,
        var scheme: String? = null,
        var auth: String? = null,
        var path: String? = null,
        var pathOption: String? = null
)
