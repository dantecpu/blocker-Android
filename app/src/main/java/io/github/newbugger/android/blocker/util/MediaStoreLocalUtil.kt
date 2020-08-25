package io.github.newbugger.android.blocker.util

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.storage.mediastore.MediaStoreUtil
import io.github.newbugger.android.storage.mediastore.MediaStoreUtil.defaultMediaStoreInputStream
import io.github.newbugger.android.storage.mediastore.MediaStoreUtil.defaultMediaStoreOutputStream
import java.io.File


@RequiresApi(29)
object MediaStoreLocalUtil {

    fun readAllText(context: Context, appName: String?, mimeType: String? = mimeTypeJson): Map<String?, String?> {
        val map = HashMap<String?, String?>()
        MediaStoreUtil.Downloads.getFolderFile(context, context.appName(appName), mimeType).forEach { file ->
            file?.uri?.let { uri ->
                context.defaultMediaStoreInputStream(uri).use {
                    map[file.relativeName.split(File.separator).last().replace(ConstantUtil.EXTENSION_JSON, "")] = it.bufferedReader().readText()
                    it.close()
                }
            }
        }
        return map
    }

    fun readText(context: Context, appName: String?, displayName: String, mimeType: String? = mimeTypeJson): String? {
        var text: String? = null
        MediaStoreUtil.Downloads.getFile(context, context.appName(appName), displayName, mimeType)?.uri?.let { uri: Uri ->
            context.defaultMediaStoreInputStream(uri).use {
                text = it.bufferedReader().readText()
                it.close()
            }
        }
        return text
    }

    fun writeText(context: Context, content: String, appName: String?, displayName: String, mimeType: String? = mimeTypeJson) {
        context.defaultMediaStoreOutputStream(MediaStoreUtil.Downloads.newFile(context, context.appName(appName), displayName, mimeType).uri).use {
            content.byteInputStream(Charsets.UTF_8).copyTo(it)
            it.close()
        }
    }

    private fun Context.appName(appName: String?): String =
            this.getString(R.string.app_name).let {
                if (appName == null) {
                    it
                } else {
                    it + File.separator + appName
                }
            }

    private const val mimeTypeJson: String = "application/json"

}
