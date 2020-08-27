package io.github.newbugger.android.storage.storageaccessframework

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.IOException


object DocumentFileKTX {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultDocumentFileInputStream(uri: Uri): BufferedInputStream =
            contentResolver.openInputStream(uri)?.buffered() ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultDocumentFileOutputStream(uri: Uri): BufferedOutputStream =
            contentResolver.openOutputStream(uri)?.buffered() ?: throw FileNotFoundException()

}
