package io.github.newbugger.android.storage.storageaccessframework.saf.defaultor

import com.google.gson.GsonBuilder


data class DDefaultSAFPreferences(
        var item: MutableList<DDefaultSAFPreferencesValues> = ArrayList()
) {
    fun item(appName: String): MutableList<DDefaultSAFPreferencesValues> {
        return item.filter { it.appName == appName }.toMutableList()
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}

data class DDefaultSAFPreferencesValues (
        var appName: String = "",
        var content: String = ""
)
