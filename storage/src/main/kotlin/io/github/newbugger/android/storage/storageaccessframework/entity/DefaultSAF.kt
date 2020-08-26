package io.github.newbugger.android.storage.storageaccessframework.entity

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import io.github.newbugger.android.storage.BuildConfig


/**
 * use Preferences as default uri value
 */

@RequiresApi(26)
class DefaultSAF(private val context: Context) {

    fun put(uri: Uri) {
        if (check(uri)) {
            val uriNew = get().let {
                if (it == null) {
                    "$uri"
                } else {
                    "$it,$uri"
                }
            }.also {
                if (BuildConfig.DEBUG) Log.e(javaClass.name, "put: $it")
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("saf", uriNew).apply()
        }
    }

    fun get(): String? =
            PreferenceManager.getDefaultSharedPreferences(context).getString("saf", null).also {
                if (BuildConfig.DEBUG) Log.e(javaClass.name, "get: $it")
            }

    fun check(uri: Uri? = null): Boolean =
            (uri == null && get() == null) ||
                    (uri != null && (get()?.split(",")?.contains(uri.toString()) == false || get() == null))

    companion object {
        val Context.defaultSAF: DefaultSAF
            get() = DefaultSAF(this)
    }

}
