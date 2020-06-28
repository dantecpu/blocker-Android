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
            else -> EControllerMethod.IFW
        }
    }

}
