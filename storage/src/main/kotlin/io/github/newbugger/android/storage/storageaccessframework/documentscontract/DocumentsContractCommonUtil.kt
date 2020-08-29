package io.github.newbugger.android.storage.storageaccessframework.documentscontract

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import java.io.FileNotFoundException
import java.io.IOException


/**
 * all those functions use Intent.getData().data Uri
 */

@RequiresApi(26)
object DocumentsContractCommonUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getFileUri(uri: Uri, documentId: String): Uri =
            DocumentsContract.buildDocumentUriUsingTree(uri, documentId)

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getTreeUri(uri: Uri): Uri =
            DocumentsContract.getTreeDocumentId(uri).let {
                DocumentsContract.buildChildDocumentsUriUsingTree(uri, it)
            }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun createDocument(contentResolver: ContentResolver, uri: Uri, displayName: String, mimeType: String): Uri =
            DocumentsContract.createDocument(contentResolver, uri, mimeType, displayName) ?: throw IOException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getDocumentId(uri: Uri): String =
            DocumentsContract.getDocumentId(uri)

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getTreeDocumentId(uri: Uri): String =
            DocumentsContract.getTreeDocumentId(uri)

    /**
     * https://stackoverflow.com/a/5254817
     *
     * Documents express their display name and MIME type as separate fields
     * so use this to get file name without extension (bool replace = true)
     * or add extension for display name (bool replace = false)
     */
    fun toFileExtension(display: String, extension: String, replace: Boolean): String =
            display.lastIndexOf(extension).let {
                if (replace) {
                    if (it < 0) {
                        display
                    } else {
                        display.substring(0, it) + display.substring(it).replaceFirst(extension, "")
                    }
                } else {
                    if (it < 0) {
                        display + extension
                    } else {
                        display
                    }
                }
            }

}
