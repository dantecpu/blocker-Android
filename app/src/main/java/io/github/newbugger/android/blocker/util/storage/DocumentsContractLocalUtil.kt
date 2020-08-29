package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.storage.storageaccessframework.documentscontract.DocumentsContractCommonUtil
import io.github.newbugger.android.storage.storageaccessframework.documentscontract.defaultor.DefaultDocumentsContractTextUtil


@RequiresApi(26)
object DocumentsContractLocalUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = mimeTypeJson): MutableMap<String?, String?> {
        val map = mutableMapOf<String?, String?>()
        DefaultDocumentsContractTextUtil.readAllText(context, appName, mimeType).filter { (packageName, text) ->
            packageName?.isNotEmpty() == true && text?.isNotEmpty() == true
        }.forEach { (filename, text) ->
            when (mimeType) {
                mimeTypeJson -> {
                    map[DocumentsContractCommonUtil.toFileExtension(filename!!, ConstantUtil.EXTENSION_JSON, true)] = text
                }
                mimeTypeXml -> {
                    map[DocumentsContractCommonUtil.toFileExtension(filename!!, ConstantUtil.EXTENSION_XML, true)] = text
                }
                else -> {
                    map[filename] = text
                }
            }
        }
        return map
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return DefaultDocumentsContractTextUtil.readText(context, appName, displayName, mimeType.findExtension(), mimeType)
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String) {
        DefaultDocumentsContractTextUtil.writeText(context, content, appName, displayName, mimeType.findExtension(), mimeType, true)
    }

    private fun String?.findExtension(): String? =
            when(this) {
                mimeTypeJson -> ConstantUtil.EXTENSION_JSON
                mimeTypeXml -> ConstantUtil.EXTENSION_XML
                else -> null
            }

    private const val mimeTypeJson: String = DefaultDocumentsContractTextUtil.mimeType_Json
    private const val mimeTypeXml: String = DefaultDocumentsContractTextUtil.mimeType_xml
    private const val mimeTypePlain: String = DefaultDocumentsContractTextUtil.mimeType_plain

}
