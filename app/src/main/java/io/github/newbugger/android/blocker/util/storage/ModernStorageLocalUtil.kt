package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.libkit.utils.ConstantUtil


@RequiresApi(30)
object ModernStorageLocalUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = mimeTypeJson): Map<String?, String?> {
        return if (context.check()) {
            MediaStoreLocalUtil.readAllText(context, appName, mimeType)
        } else {
            DocumentFileLocalUtil.readAllText(context, appName, ConstantUtil.NAME_APP_NAME_DEFAULT, mimeType)
        }
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return if (context.check()) {
            MediaStoreLocalUtil.readText(context, appName, displayName, mimeType)
        } else {
            DocumentFileLocalUtil.readText(context, appName, ConstantUtil.NAME_APP_NAME_DEFAULT, displayName, mimeType)
        }
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String = mimeTypeJson) {
        if (context.check()) {
            MediaStoreLocalUtil.writeText(context, content, appName, displayName, mimeType)
        } else {
            DocumentFileLocalUtil.writeText(context, content, appName, ConstantUtil.NAME_APP_NAME_DEFAULT, displayName, mimeType)
        }
    }

    private fun Context.check(): Boolean =
            PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_media_key), true)

    private const val mimeTypeJson: String = MediaStoreLocalUtil.mimeTypeJson
    const val mimeTypeXml: String = MediaStoreLocalUtil.mimeTypeXml
    const val mimeTypePlain: String = MediaStoreLocalUtil.mimeTypePlain

}
