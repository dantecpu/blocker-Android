package io.github.newbugger.android.storage.storageaccessframework.documentscontract.defaultor

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultSAF
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultSAF.Companion.defaultSAF
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultSAFUnavailableException
import io.github.newbugger.android.storage.storageaccessframework.documentscontract.DocumentsContractUtil


@RequiresApi(26)
object DefaultDocumentsContractUtil {

    fun listFiles(context: Context, appName: String, mimeType: String? = null): MutableMap<String?, Uri?> {
        return DocumentsContractUtil.listFiles(context, context.defaultUri(appName), mimeType)
    }

    fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): Uri {
        return DocumentsContractUtil.getFile(context, context.defaultUri(appName), displayName, mimeType)
    }

    fun newFile(context: Context, appName: String, displayName: String, mimeType: String, override: Boolean = false): Uri {
        return DocumentsContractUtil.newFile(context, context.defaultUri(appName), displayName, mimeType, override)
    }

    private fun Context.defaultUri(appName: String): Uri =
            this.defaultSAF().getUri(appName) ?: throw DefaultSAFUnavailableException()

    private fun Context.defaultSAF(): DefaultSAF =
            this.defaultSAF

}
