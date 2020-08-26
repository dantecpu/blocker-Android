package io.github.newbugger.android.blocker.util

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.storage.mediastore.entity.MediaStoreTextUtil
import java.io.File


@RequiresApi(29)
object MediaStoreLocalUtil {

    fun readAllText(context: Context, appName: String?, mimeType: String? = mimeTypeJson): Map<String?, String?> {
        val map = HashMap<String?, String?>()
        MediaStoreTextUtil.readAllText(context, context.appName(appName), mimeType).forEach { (filename, text) ->
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

    fun readText(context: Context, appName: String?, displayName: String, mimeType: String? = mimeTypeJson): String? {
        return MediaStoreTextUtil.readText(context, context.appName(appName), displayName.displayName(mimeType), mimeType)
    }

    fun writeText(context: Context, content: String, appName: String?, displayName: String, mimeType: String? = mimeTypeJson) {
        MediaStoreTextUtil.writeText(context, content, context.appName(appName), displayName.displayName(mimeType), mimeType)
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
    const val mimeTypeXml: String = MediaStoreTextUtil.mimeType_xml
    const val mimeTypePlain: String = MediaStoreTextUtil.mimeType_plain

}
