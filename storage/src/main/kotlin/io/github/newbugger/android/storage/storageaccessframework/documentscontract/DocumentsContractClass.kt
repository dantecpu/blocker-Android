package io.github.newbugger.android.storage.storageaccessframework.documentscontract

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import java.io.FileNotFoundException
import java.io.IOException


/**
 * https://developer.android.com/reference/android/provider/DocumentsContract.html
 *
 * All client apps must hold a valid URI permission grant to access documents,
 * typically issued when a user makes a selection through
 * Intent#ACTION_OPEN_DOCUMENT, Intent#ACTION_CREATE_DOCUMENT, or Intent#ACTION_OPEN_DOCUMENT_TREE
 */

@RequiresApi(26)
class DocumentsContractClass(context: Context) {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun listFiles(treeUri: Uri, mimeType: String? = null): MutableMap<String?, Uri?> {
        val collection = mutableMapOf<String?, Uri?>()
        queryFolderFile(DocumentsContractCommonUtil.getTreeUri(treeUri), mimeType).forEach {
            collection[it?.displayName] = it?.uri
        }
        return collection
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getFile(treeUri: Uri, displayName: String, mimeType: String? = null): Uri {
        return queryFile(DocumentsContractCommonUtil.getTreeUri(treeUri), displayName, mimeType)?.uri ?: throw FileNotFoundException()
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun newFile(treeUri: Uri, displayName: String, mimeType: String, override: Boolean = false): Uri {
        if (override) queryFile(DocumentsContractCommonUtil.getTreeUri(treeUri), displayName, mimeType)?.delete()
        return DocumentsContractCommonUtil.createDocument(contentResolver, DocumentsContractCommonUtil.getTreeUri(treeUri), displayName, mimeType)
    }

    /**
     * https://stackoverflow.com/a/55553125
     * The file system provider doesn't really support filtering,
     * The only choice is to get all rows and filter yourself.
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun queryFolderFile(uri: Uri, mimeType: String?): MutableList<DocumentsContractFile?> {
        val collection = mutableListOf<DocumentsContractFile?>()

        val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        val selection = null
        val selectionArgs = null
        val sortOrder = "${DocumentsContract.Document.COLUMN_MIME_TYPE} ASC"

        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val name = cursor.getString(nameColumn)
                val mime = cursor.getString(mimeColumn)
                if (mimeType == null || mimeType == mime) {
                    collection.add(DocumentsContractFile(id, name, uri, contentResolver))
                }
            }
        }

        return collection
    }

    /**
     * https://stackoverflow.com/a/55553125
     * The file system provider doesn't really support filtering,
     * The only choice is to get all rows and filter yourself.
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun queryFile(uri: Uri, displayName: String, mimeType: String?): DocumentsContractFile? {
        val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        val selection = null
        val selectionArgs = null
        val sortOrder = "${DocumentsContract.Document.COLUMN_DISPLAY_NAME} ASC"

        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val name = cursor.getString(nameColumn)
                val mime = cursor.getString(mimeColumn)
                if (name == displayName && (mimeType == null || mimeType == mime)) {
                    return DocumentsContractFile(id, name, uri, contentResolver)
                }
            }
        }

        return null
    }

    private val contentResolver: ContentResolver = context.contentResolver

    private data class DocumentsContractFile(private val id: String,
                                     private val data: String,
                                     private val treeUri: Uri,
                                     private val contentResolver: ContentResolver) {
        val uri: Uri = DocumentsContractCommonUtil.getFileUri(treeUri, id)
        val displayName: String = data

        @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
        fun delete() {
            DocumentsContract.deleteDocument(contentResolver, uri)
        }
    }

    companion object {
        val Context.documentsContractClass: DocumentsContractClass
            get() = DocumentsContractClass(this)
    }

}
