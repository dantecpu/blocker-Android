package io.github.newbugger.android.storage.mediastore.entity

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.mediastore.MediaStoreKTX.defaultMediaStoreInputStream
import io.github.newbugger.android.storage.mediastore.MediaStoreKTX.defaultMediaStoreOutputStream
import io.github.newbugger.android.storage.mediastore.DefaultMediaStore
import io.github.newbugger.android.storage.mediastore.DefaultMediaStore.Companion.defaultMediaStore


@RequiresApi(29)
object MediaStoreTextUtil {

    fun readAllText(context: Context, appName: String, mimeType: String? = null): Map<String?, String?> {
        val map = HashMap<String?, String?>()
        context.defaultMediaStore().Downloads().getFolderFile(appName, mimeType).forEach { file ->
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
        context.defaultMediaStore().Downloads().getFile(appName, displayName, mimeType)?.uri?.let { uri ->
            context.defaultMediaStoreInputStream(uri).use {
                text = it.bufferedReader().readText()
                it.close()
            }
        }
        return text
    }

    fun writeText(context: Context, content: String, appName: String, displayName: String, mimeType: String? = null) {
        context.defaultMediaStoreOutputStream(context.defaultMediaStore().Downloads().newFile(appName, displayName, mimeType).uri).use {
            content.byteInputStream(Charsets.UTF_8).copyTo(it)
            it.close()
        }
    }

    private fun Context.defaultMediaStore(): DefaultMediaStore =
            this.defaultMediaStore

    const val mimeType_plain: String = "text/plain"
    const val mimeType_Json: String = "application/json"
    const val mimeType_xml: String = "application/xml"

}
