package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.entity.DocumentFileCommonUtil
import io.github.newbugger.android.storage.storageaccessframework.entity.DocumentFileTextUtil


@RequiresApi(26)
object DocumentFileLocalUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = mimeTypeJson): MutableMap<String?, String?> {
        val map = mutableMapOf<String?, String?>()
        DocumentFileTextUtil.readAllText(context, appName, mimeType).forEach { (filename, text) ->
            map[filename] = text
        }
        return map
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return DocumentFileTextUtil.readText(context, appName, displayName, mimeType)
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String) {
        DocumentFileTextUtil.writeText(context, content, appName, displayName, mimeType, true)
    }

    fun getDirectoryName(context: Context, uri: Uri): String =
            DocumentFileCommonUtil.getDirectoryName(context, uri)

    fun getDirectoryUri(context: Context, uri: Uri): Uri =
            DocumentFileCommonUtil.getDirectoryUri(context, uri)

    private const val mimeTypeJson: String = DocumentFileTextUtil.mimeType_Json
    private const val mimeTypeXml: String = DocumentFileTextUtil.mimeType_xml
    private const val mimeTypePlain: String = DocumentFileTextUtil.mimeType_plain

}
