package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.storage.storageaccessframework.entity.DocumentFileTextUtil


@RequiresApi(26)
object ModernStorageLocalUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = mimeTypeJson): MutableMap<String?, String?> {
        return if (check(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStoreLocalUtil.readAllText(context, appName, mimeType)
        } else {
            DocumentFileLocalUtil.readAllText(context, appName, mimeType)
        }
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return if (check(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStoreLocalUtil.readText(context, appName, displayName, mimeType)
        } else {
            DocumentFileLocalUtil.readText(context, appName, displayName, mimeType)
        }
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String = mimeTypeJson) {
        if (check(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStoreLocalUtil.writeText(context, content, appName, displayName, mimeType)
        } else {
            DocumentFileLocalUtil.writeText(context, content, appName, displayName, mimeType)
        }
    }

    fun check(context: Context): Boolean =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_media_key), true)

    fun replaceStringLast(string: String, from: String, to: String): String =
            string.lastIndexOf(from).let {
                if (it < 0) {
                    string
                } else {
                    string.substring(it).replaceFirst(from, to)
                }
            }

    private const val mimeTypeJson: String = DocumentFileTextUtil.mimeType_Json
    const val mimeTypeXml: String = DocumentFileTextUtil.mimeType_xml
    const val mimeTypePlain: String = DocumentFileTextUtil.mimeType_plain

}
