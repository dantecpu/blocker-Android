package com.merxury.blocker.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.merxury.blocker.R
import com.merxury.blocker.core.root.EControllerMethod

object PreferenceUtil {
    fun getControllerType(context: Context): EControllerMethod {
        // Magic value, but still use it.
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return when (pref.getString(context.getString(R.string.key_pref_controller_type), context.getString(R.string.key_pref_controller_type_default_value))) {
            "pm" -> EControllerMethod.PM
            "shizuku" -> EControllerMethod.SHIZUKU
            else -> EControllerMethod.IFW
        }
    }
}
