package io.github.newbugger.android.storage.storageaccessframework.saf.defaultor

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import io.github.newbugger.android.storage.BuildConfig


/**
 * use Preferences as default uri value
 */

@RequiresApi(26)
class DefaultSAF(private val context: Context) {

    fun put(appName: String, content: Uri) {
        defaultSAFPreferences.edit(appName, content.toString()).also {
            if (BuildConfig.DEBUG) Log.e(javaClass.name, "put: $appName $content")
        }
    }

    fun getUri(appName: String): Uri? =
            defaultSAFPreferences.uri(appName).also {
                if (BuildConfig.DEBUG) Log.e(javaClass.name, "get: $appName $it")
            }

    fun getString(appName: String): String? =
            defaultSAFPreferences.string(appName).also {
                if (BuildConfig.DEBUG) Log.e(javaClass.name, "get: $appName $it")
            }

    fun check(appName: String): Boolean =
            getString(appName)?.isNotEmpty() == true

    private val defaultSAFPreferences: DefaultSAFPreferences =
            DefaultSAFPreferences(sharedPreferences)

    private val sharedPreferences: SharedPreferences get() =
            context.getSharedPreferences(defaultPreferencesFlag, Context.MODE_PRIVATE)

    private data class DefaultSAFPreferences(private val sharedPreferences: SharedPreferences) {
        fun string(appName: String): String? { return json()?.item(appName)?.let { if (it.isNotEmpty()) { it[0].content } else { null } } }
        fun uri(appName: String): Uri? { return (string(appName) ?: return null).let { Uri.parse(it) } }

        private fun string(): String? = sharedPreferences.getString(defaultSAFFlag, null)
        private fun json(): DDefaultSAFPreferences? = Gson().fromJson<DDefaultSAFPreferences>(string(), DDefaultSAFPreferences::class.java) ?: null
        private fun value(): DDefaultSAFPreferences = DDefaultSAFPreferences()

        fun edit(appName: String, content: String) {
            val json = json()
            if (json?.item(appName)?.isNotEmpty() == true) {
                val edit = json.apply { item(appName).forEach { it.content = content } }.toString()
                sharedPreferences.edit().putString(defaultSAFFlag, edit).apply()
            } else {
                val add = (json ?: value()).apply { item.add(DDefaultSAFPreferencesValues(appName = appName, content = content)) }.toString()
                sharedPreferences.edit().putString(defaultSAFFlag, add).apply()
            }
        }

        fun delete(appName: String) {
            val json = json()
            val delete = value().apply {
                if (json?.item?.isNotEmpty() == true) item.addAll(json.item)
                item(appName).forEach { item.remove(it) }
            }.toString()
            sharedPreferences.edit().putString(defaultSAFFlag, delete).apply()
        }
    }

    companion object {
        private const val defaultSAFFlag: String = "saf"
        private const val defaultPreferencesFlag: String = defaultSAFFlag

        val Context.defaultSAF: DefaultSAF
            get() = DefaultSAF(this)
    }

}
