package io.github.newbugger.android.storage.storageaccessframework.documentfile

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import java.io.IOException


/**
 * https://developer.android.com/reference/androidx/documentfile/provider/DocumentFile
 *
 * (1) all those functions use Intent.getData().data Uri
 * fromSingleUri() / fromTreeUri()
 * uri: the Intent.getData() from a successful Intent.ACTION_OPEN_DOCUMENT or Intent.ACTION_CREATE_DOCUMENT request
 * eg: Android 10 content://com.android.providers.downloads.documents/tree/msd%3A82
 * eg: Android 11 content://com.android.externalstorage.documents/tree/primary%3ADownload%2FBlocker%2Frule
 * but DocumentFile class uses DocumentUri: if given Uri is backed by a DocumentsProvider
 * eg: content://com.android.externalstorage.documents/tree/primary%3ADownload%2FBlocker/document/primary%3ADownload%2FBlocker%2Frule
 *
 * (2) did not use file extensions into the display name
 * Documents express their display name and MIME type as separate fields, instead of relying on file extensions.
 * Some documents providers may still choose to append extensions to their display names, but that's an implementation detail.
 * but seems getName(): Return the display name of this document gives file extension still ?
 *
 */

@RequiresApi(26)
object DocumentFileUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun findFile(context: Context, uri: Uri, displayName: String, extension: String? = null, mimeType: String? = null): DocumentFile? =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                it?.listFiles()
            }.let {
                it?.filter { i -> ((extension == null && i.name?.startsWith(displayName) == true) || (extension != null && i.name == displayName + extension)) && i.type == mimeType }
            }.let {
                if (it?.isNotEmpty() == true) { it[0] } else { null }
            }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun listFiles(context: Context, uri: Uri): MutableList<DocumentFile?> =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                it?.listFiles()
            }.let {
                it?.filter { i -> i.isFile }?.toMutableList() ?: mutableListOf()
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
    fun newFile(context: Context, uri: Uri, displayName: String, extension: String? = null, mimeType: String, override: Boolean): DocumentFile =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                if (override) findFile(context, uri, displayName, extension, mimeType)?.delete()
                it?.createFile(mimeType, displayName)
            } ?: throw IOException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun newDirectory(context: Context, uri: Uri, displayName: String): DocumentFile =
            uri.let {
                DocumentFile.fromTreeUri(context, it)
            }.let {
                it?.createDirectory(displayName)
            } ?: throw IOException()

}
