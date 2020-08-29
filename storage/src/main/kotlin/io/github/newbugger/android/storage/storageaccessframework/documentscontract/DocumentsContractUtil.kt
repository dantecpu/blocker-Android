package io.github.newbugger.android.storage.storageaccessframework.documentscontract

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.documentscontract.DocumentsContractClass.Companion.documentsContractClass


@RequiresApi(26)
object DocumentsContractUtil {

    fun listFiles(context: Context, treeUri: Uri, mimeType: String? = null): MutableMap<String?, Uri?> {
        return context.documentsContractClass().listFiles(treeUri, mimeType)
    }

    fun getFile(context: Context, treeUri: Uri, displayName: String, mimeType: String? = null): Uri {
        return context.documentsContractClass().getFile(treeUri, displayName, mimeType)
    }

    fun newFile(context: Context, treeUri: Uri, displayName: String, mimeType: String, override: Boolean = false): Uri {
        return context.documentsContractClass().newFile(treeUri, displayName, mimeType, override)
    }

    private fun Context.documentsContractClass(): DocumentsContractClass =
            this.documentsContractClass

}
