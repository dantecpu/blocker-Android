package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.storage.storageaccessframework.documentfile.defaultor.DefaultDocumentFileTextUtil


/**
 * choose one of DocumentFileLocalUtil (DocumentFile) or DocumentsContractLocalUtil (DocumentsContract, a bit performance fast)
 */

@RequiresApi(26)
object ModernStorageLocalUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = mimeTypeJson): MutableMap<String?, String?> {
        return if (check(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStoreLocalUtil.readAllText(context, appName, mimeType)
        } else {
            DocumentsContractLocalUtil.readAllText(context, appName, mimeType)
            // DocumentFileLocalUtil.readAllText(context, appName, mimeType)
        }
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return if (check(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStoreLocalUtil.readText(context, appName, displayName, mimeType)
        } else {
            DocumentsContractLocalUtil.readText(context, appName, displayName, mimeType)
            // DocumentFileLocalUtil.readText(context, appName, displayName, mimeType)
        }
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String = mimeTypeJson) {
        if (check(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStoreLocalUtil.writeText(context, content, appName, displayName, mimeType)
        } else {
            DocumentsContractLocalUtil.writeText(context, content, appName, displayName, mimeType)
            // DocumentFileLocalUtil.writeText(context, content, appName, displayName, mimeType)
        }
    }

    fun check(context: Context): Boolean =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_media_key), true)

    private const val mimeTypeJson: String = DefaultDocumentFileTextUtil.mimeType_Json
    const val mimeTypeXml: String = DefaultDocumentFileTextUtil.mimeType_xml
    const val mimeTypePlain: String = DefaultDocumentFileTextUtil.mimeType_plain

}
