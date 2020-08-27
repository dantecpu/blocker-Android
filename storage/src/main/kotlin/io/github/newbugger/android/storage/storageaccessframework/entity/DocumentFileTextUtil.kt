package io.github.newbugger.android.storage.storageaccessframework.entity

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultDocumentFileStreamUtil


@RequiresApi(26)
object DocumentFileTextUtil {

    fun readAllText(context: Context, appName: String, appNameDefault: String): Map<String?, String?> {
        val map = HashMap<String?, String?>()
        DefaultDocumentFileStreamUtil.fileAllInputStream(context, appName, appNameDefault).forEach { (filename, inputStream) ->
            map[filename] = inputStream.bufferedReader().readText()
            inputStream.close()
        }
        return map
    }

    fun readText(context: Context, appName: String, appNameDefault: String, displayName: String): String? {
        var text: String? = null
        DefaultDocumentFileStreamUtil.fileInputStream(context, appName, appNameDefault, displayName).use {
            text = it.bufferedReader().readText()
            it.close()
        }
        return text
    }

    fun writeText(context: Context, content: String, appName: String, appNameDefault: String, displayName: String, mimeType: String, override: Boolean = false) {
        DefaultDocumentFileStreamUtil.fileOutputStream(context, appName, appNameDefault, displayName, mimeType, override).use {
            content.byteInputStream(Charsets.UTF_8).copyTo(it)
            it.close()
        }
    }

    const val mimeType_plain: String = "text/plain"
    const val mimeType_Json: String = "application/json"
    const val mimeType_xml: String = "application/xml"

}
