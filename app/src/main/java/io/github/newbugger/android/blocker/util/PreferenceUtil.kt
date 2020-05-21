package io.github.newbugger.android.blocker.util

import android.content.Context
import androidx.preference.PreferenceManager
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.core.root.EControllerMethod

object PreferenceUtil {
    fun getControllerType(context: Context): EControllerMethod {
        // Magic value, but still use it.
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return when (pref.getString(context.getString(R.string.key_pref_controller_type), context.getString(R.string.key_pref_controller_type_default_value))) {
            "ifw" -> EControllerMethod.IFW
            "pm" -> EControllerMethod.PM
            "shizuku" -> EControllerMethod.SHIZUKU
            else -> EControllerMethod.SHIZUKU
        }
    }

    fun checkShizukuType(context: Context): Boolean {
        return getControllerType(context) == EControllerMethod.SHIZUKU
    }

    // TODO: preference for Transact mode, when the ListPreference is ready
    fun checkTransactType(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getString(context.getString(R.string.key_pref_transact_type), context.getString(R.string.key_pref_transact_type_default_value)) == "wrapper"
    }

}
