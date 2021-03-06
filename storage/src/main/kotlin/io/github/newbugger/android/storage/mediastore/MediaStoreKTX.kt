package io.github.newbugger.android.storage.mediastore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.IOException


object MediaStoreKTX {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreDisplayName(uri: Uri): String {
        require(uri.scheme == ContentResolver.SCHEME_CONTENT) { "not ${ContentResolver.SCHEME_CONTENT} scheme: $uri" }
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
    fun Context.defaultMediaStoreInputStream(uri: Uri): BufferedInputStream =
            contentResolver.openInputStream(uri)?.buffered() ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreOutputStream(uri: Uri): BufferedOutputStream =
            contentResolver.openOutputStream(uri)?.buffered() ?: throw FileNotFoundException()

}
