package io.github.newbugger.android.storage.mediastore.entity

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.mediastore.MediaStoreKTX.defaultMediaStoreInputStream
import io.github.newbugger.android.storage.mediastore.MediaStoreKTX.defaultMediaStoreOutputStream
import io.github.newbugger.android.storage.mediastore.MediaStoreUtil


@RequiresApi(30)
object MediaStoreTextUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = null): Map<String?, String?> {
        val map = HashMap<String?, String?>()
        MediaStoreUtil.Downloads.getFolderFile(context, appName, mimeType).forEach { file ->
            file?.uri?.let { uri ->
                context.defaultMediaStoreInputStream(uri).use {
                    map[file.relativeName] = it.bufferedReader().readText()
                    it.close()
                }
            }
        }
        return map
    }

    fun readText(context: Context, appName: String, displayName: String, mimeType: String? = null): String? {
        var text: String? = null
        MediaStoreUtil.Downloads.getFile(context, appName, displayName, mimeType)?.uri?.let { uri ->
            context.defaultMediaStoreInputStream(uri).use {
                text = it.bufferedReader().readText()
                it.close()
            }
        }
        return text
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String? = null, override: Boolean = false) {
        context.defaultMediaStoreOutputStream(MediaStoreUtil.Downloads.newFile(context, appName, displayName, mimeType, override).uri).use {
            content.byteInputStream(Charsets.UTF_8).copyTo(it)
            it.close()
        }
    }

    const val mimeType_plain: String = "text/plain"
    const val mimeType_Json: String = "application/json"
    const val mimeType_xml: String = "application/xml"

}
