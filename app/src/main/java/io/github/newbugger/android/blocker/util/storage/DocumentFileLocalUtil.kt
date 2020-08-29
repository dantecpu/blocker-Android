package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.storage.storageaccessframework.documentfile.entity.DocumentFileCommonUtil
import io.github.newbugger.android.storage.storageaccessframework.documentfile.defaultor.DefaultDocumentFileTextUtil


@RequiresApi(26)
object DocumentFileLocalUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = mimeTypeJson): MutableMap<String?, String?> {
        val map = mutableMapOf<String?, String?>()
        DefaultDocumentFileTextUtil.readAllText(context, appName, mimeType).filter { (packageName, text) ->
            packageName?.isNotEmpty() == true && text?.isNotEmpty() == true
        }.forEach { (filename, text) ->
            when (mimeType) {
                mimeTypeJson -> {
                    map[DocumentFileCommonUtil.toFileExtension(filename!!, ConstantUtil.EXTENSION_JSON, true)] = text
                }
                mimeTypeXml -> {
                    map[DocumentFileCommonUtil.toFileExtension(filename!!, ConstantUtil.EXTENSION_XML, true)] = text
                }
                else -> {
                    map[filename] = text
                }
            }
        }
        return map
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return DefaultDocumentFileTextUtil.readText(context, appName, displayName, mimeType)
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String) {
        DefaultDocumentFileTextUtil.writeText(context, content, appName, displayName, mimeType, true)
    }

    fun getDirectoryName(context: Context, uri: Uri): String =
            DocumentFileCommonUtil.getDirectoryName(context, uri)

    fun getDirectoryUri(context: Context, uri: Uri): Uri =
            DocumentFileCommonUtil.getDirectoryUri(context, uri)

    private const val mimeTypeJson: String = DefaultDocumentFileTextUtil.mimeType_Json
    private const val mimeTypeXml: String = DefaultDocumentFileTextUtil.mimeType_xml
    private const val mimeTypePlain: String = DefaultDocumentFileTextUtil.mimeType_plain

}
