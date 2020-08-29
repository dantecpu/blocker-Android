package io.github.newbugger.android.storage.storageaccessframework.documentfile.defaultor

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.saf.SAFKTX.defaultSAFInputStream
import io.github.newbugger.android.storage.storageaccessframework.saf.SAFKTX.defaultSAFOutputStream


@RequiresApi(26)
object DefaultDocumentFileTextUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = null): MutableMap<String?, String?> {
        val map = mutableMapOf<String?, String?>()
        DefaultDocumentFileUtil.listFiles(context, appName, mimeType).filter { (filename, uri) ->
            filename?.isNotEmpty() == true && uri.toString().isNotEmpty()
        }.forEach { (filename, uri) ->
            context.defaultSAFInputStream(uri!!).use {
                map[filename] = it.bufferedReader().readText()
                it.close()
            }
        }
        return map
    }

    fun readText(context: Context, appName: String, displayName: String, extension: String? = null, mimeType: String? = null): String? {
        var text: String? = null
        DefaultDocumentFileUtil.getFile(context, appName, displayName, extension, mimeType).let { uri ->
            context.defaultSAFInputStream(uri).use {
                text = it.bufferedReader().readText()
                it.close()
            }
        }
        return text
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, extension: String? = null, mimeType: String, override: Boolean = false) {
        DefaultDocumentFileUtil.newFile(context, appName, displayName, extension, mimeType, override).let { uri ->
            context.defaultSAFOutputStream(uri).use {
                content.byteInputStream(Charsets.UTF_8).copyTo(it)
                it.close()
            }
        }
    }

    const val mimeType_plain: String = "text/plain"
    const val mimeType_Json: String = "application/json"
    const val mimeType_xml: String = "application/xml"

}
