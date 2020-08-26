package io.github.newbugger.android.storage.mediastore

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.io.IOException


object MediaStoreKTX {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreDisplayName(uri: Uri): String {
        require(uri.scheme == "content") { "Uri lacks 'content' scheme: $uri" }
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val displayNameColumn = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                return cursor.getString(displayNameColumn)
            }
        }
        return uri.toString()
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreInputStream(uri: Uri) =
            contentResolver.openInputStream(uri)?.buffered() ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreOutputStream(uri: Uri) =
            contentResolver.openOutputStream(uri)?.buffered() ?: throw FileNotFoundException()

}
