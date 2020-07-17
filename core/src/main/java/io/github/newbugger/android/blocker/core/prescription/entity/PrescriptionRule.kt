package io.github.newbugger.android.blocker.core.prescription.entity


data class PrescriptionRule(
        var packageName: String = "",
        var versionCode: Int = 0,
        var info: MutableList<PrescriptionInfo> = ArrayList()
)
