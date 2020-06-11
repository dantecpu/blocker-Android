package io.github.newbugger.android.blocker.core.shizuku.util

import android.content.Context
import androidx.preference.PreferenceManager


object Preference {

    fun checkTransactType(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getString("pref_transactType", "wrapper") == "wrapper"
    }

}
