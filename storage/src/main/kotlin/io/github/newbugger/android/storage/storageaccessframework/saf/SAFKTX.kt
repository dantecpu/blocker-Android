package io.github.newbugger.android.storage.storageaccessframework.saf

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.IOException


object SAFKTX {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultSAFInputStream(uri: Uri): BufferedInputStream =
            contentResolver.openInputStream(uri)?.buffered() ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultSAFOutputStream(uri: Uri): BufferedOutputStream =
            contentResolver.openOutputStream(uri)?.buffered() ?: throw FileNotFoundException()

}
