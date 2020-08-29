package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.blocker.BuildConfig
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.storage.mediastore.entity.MediaStoreTextUtil
import io.github.newbugger.android.storage.storageaccessframework.entity.DocumentFileCommonUtil
import java.io.File


@RequiresApi(30)
object MediaStoreLocalUtil {

    fun readAllText(context: Context, appName: String?, mimeType: String?): MutableMap<String?, String?> {
        val map = mutableMapOf<String?, String?>()
        MediaStoreTextUtil.readAllText(context, context.appName(appName), mimeType, BuildConfig.APPLICATION_ID).filter { (packageName, text) ->
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

    fun readText(context: Context, appName: String?, displayName: String, mimeType: String?): String? {
        return MediaStoreTextUtil.readText(context, context.appName(appName), displayName.displayName(mimeType), mimeType)
    }

    fun writeText(context: Context, content: String, appName: String?, displayName: String, mimeType: String?) {
        MediaStoreTextUtil.writeText(context, content, context.appName(appName), displayName.displayName(mimeType), mimeType, true)
    }

    private fun String.displayName(mimeType: String?): String =
            when(mimeType) {
                mimeTypeJson -> this + ConstantUtil.EXTENSION_JSON
                mimeTypeXml -> this + ConstantUtil.EXTENSION_XML
                else -> this
            }

    private fun Context.appName(appName: String?): String =
            this.getString(R.string.app_name).let {
                if (appName == null) {
                    it
                } else {
                    it + File.separator + appName
                }
            }

    private const val mimeTypeJson: String = MediaStoreTextUtil.mimeType_Json
    private const val mimeTypeXml: String = MediaStoreTextUtil.mimeType_xml
    private const val mimeTypePlain: String = MediaStoreTextUtil.mimeType_plain

}
