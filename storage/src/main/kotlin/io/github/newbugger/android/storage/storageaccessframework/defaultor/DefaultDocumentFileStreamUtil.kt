package io.github.newbugger.android.storage.storageaccessframework.defaultor

import android.content.Context
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.DocumentFileKTX.defaultDocumentFileInputStream
import io.github.newbugger.android.storage.storageaccessframework.DocumentFileKTX.defaultDocumentFileOutputStream
import java.io.BufferedInputStream


@RequiresApi(26)
object DefaultDocumentFileStreamUtil {

    fun fileAllInputStream(context: Context, appName: String, appNameDefault: String): Map<String, BufferedInputStream> {
        val map = hashMapOf<String, BufferedInputStream>()
        DefaultDocumentFileUtil.listFiles(context, appName, appNameDefault).forEach { (filename, uri) ->
            map[filename] = context.defaultDocumentFileInputStream(uri)
        }
        return map
    }

    fun fileInputStream(context: Context, appName: String, appNameDefault: String, displayName: String) =
            context.defaultDocumentFileInputStream(DefaultDocumentFileUtil.getFile(context, appName, appNameDefault, displayName))

    fun fileOutputStream(context: Context, appName: String, appNameDefault: String, displayName: String, mimeType: String, override: Boolean = false) =
            context.defaultDocumentFileOutputStream(DefaultDocumentFileUtil.newFile(context, appName, appNameDefault, displayName, mimeType, override))

}
