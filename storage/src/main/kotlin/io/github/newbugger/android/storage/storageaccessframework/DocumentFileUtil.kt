package io.github.newbugger.android.storage.storageaccessframework

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import java.io.IOException


/**
 * https://developer.android.com/reference/androidx/documentfile/provider/DocumentFile
 */

@RequiresApi(26)
object DocumentFileUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun findFile(context: Context, uri: Uri, displayName: String): DocumentFile =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                it?.findFile(displayName)
            } ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun listFiles(context: Context, uri: Uri): Array<DocumentFile>? =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                it?.listFiles()
            }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getFile(context: Context, uri: Uri): DocumentFile =
            uri.let {
                DocumentFile.fromSingleUri(context, it)
            } ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getDirectory(context: Context, uri: Uri): DocumentFile =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            } ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun newFile(context: Context, uri: Uri, displayName: String, mimeType: String, override: Boolean): DocumentFile =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                if (override && it?.exists() == true) it.delete()
                it?.createFile(mimeType, displayName)
            } ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun newDirectory(context: Context, uri: Uri, displayName: String): DocumentFile =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                it?.createDirectory(displayName)
            } ?: throw FileNotFoundException()

}
