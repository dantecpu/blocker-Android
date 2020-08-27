package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.storage.storageaccessframework.entity.DocumentFileTextUtil
import java.io.File


@RequiresApi(26)
object DocumentFileLocalUtil {

    fun readAllText(context: Context, appName: String, appNameDefault: String, mimeType: String? = mimeTypeJson): Map<String?, String?> {
        val map = HashMap<String?, String?>()
        DocumentFileTextUtil.readAllText(context, appName, appNameDefault).forEach { (filename, text) ->
            when (mimeType) {
                mimeTypeJson -> {
                    map[filename?.split(File.separator)?.last()?.replace(ConstantUtil.EXTENSION_JSON, "")] = text
                }
                mimeTypeXml -> {
                    map[filename?.split(File.separator)?.last()?.replace(ConstantUtil.EXTENSION_XML, "")] = text
                }
                else -> {
                    return@forEach
                }
            }
        }
        return map
    }

    fun readText(context: Context, appName: String, appNameDefault: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return DocumentFileTextUtil.readText(context, appName, appNameDefault, displayName.displayName(mimeType))
    }

    fun writeText(context: Context, content: String, appName: String, appNameDefault: String, displayName: String, mimeType: String = mimeTypeJson) {
        DocumentFileTextUtil.writeText(context, content, appName, appNameDefault, displayName.displayName(mimeType), mimeType, true)
    }

    private fun String.displayName(mimeType: String?): String =
            when(mimeType) {
                mimeTypeJson -> this + ConstantUtil.EXTENSION_JSON
                mimeTypeXml -> this + ConstantUtil.EXTENSION_XML
                else -> this
            }

    private const val mimeTypeJson: String = DocumentFileTextUtil.mimeType_Json
    private const val mimeTypeXml: String = DocumentFileTextUtil.mimeType_xml
    private const val mimeTypePlain: String = DocumentFileTextUtil.mimeType_plain

}
