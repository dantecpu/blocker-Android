package io.github.newbugger.android.storage.storageaccessframework.entity

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.DocumentFileKTX.defaultDocumentFileInputStream
import io.github.newbugger.android.storage.storageaccessframework.DocumentFileKTX.defaultDocumentFileOutputStream
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultDocumentFileUtil


@RequiresApi(26)
object DocumentFileTextUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = null): MutableMap<String?, String?> {
        val map = mutableMapOf<String?, String?>()
        DefaultDocumentFileUtil.listFiles(context, appName, mimeType).filter { (filename, uri) ->
            filename?.isNotEmpty() == true && uri.toString().isNotEmpty()
        }.forEach { (filename, uri) ->
            context.defaultDocumentFileInputStream(uri!!).use {
                map[filename] = it.bufferedReader().readText()
                it.close()
            }
        }
        return map
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = null): String? {
        var text: String? = null
        DefaultDocumentFileUtil.getFile(context, appName, displayName, mimeType).let { uri ->
            context.defaultDocumentFileInputStream(uri).use {
                text = it.bufferedReader().readText()
                it.close()
            }
        }
        return text
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String, override: Boolean = false) {
        DefaultDocumentFileUtil.newFile(context, appName, displayName, mimeType, override).let { uri ->
            context.defaultDocumentFileOutputStream(uri).use {
                content.byteInputStream(Charsets.UTF_8).copyTo(it)
                it.close()
            }
        }
    }

    const val mimeType_plain: String = "text/plain"
    const val mimeType_Json: String = "application/json"
    const val mimeType_xml: String = "application/xml"

}
