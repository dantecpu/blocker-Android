package io.github.newbugger.android.storage.storageaccessframework.documentscontract.defaultor

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAF
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAF.Companion.defaultSAF
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAFUnavailableException
import io.github.newbugger.android.storage.storageaccessframework.documentscontract.DocumentsContractUtil


@RequiresApi(26)
object DefaultDocumentsContractUtil {

    fun listFiles(context: Context, appName: String, mimeType: String? = null): MutableMap<String?, Uri?> {
        return DocumentsContractUtil.listFiles(context, context.defaultUri(appName), mimeType)
    }

    fun getFile(context: Context, appName: String, displayName: String, extension: String? = null, mimeType: String? = null): Uri {
        return DocumentsContractUtil.getFile(context, context.defaultUri(appName), displayName, extension, mimeType)
    }

    fun newFile(context: Context, appName: String, displayName: String, extension: String? = null, mimeType: String, override: Boolean = false): Uri {
        return DocumentsContractUtil.newFile(context, context.defaultUri(appName), displayName, extension, mimeType, override)
    }

    private fun Context.defaultUri(appName: String): Uri =
            this.defaultSAF().getUri(appName) ?: throw DefaultSAFUnavailableException()

    private fun Context.defaultSAF(): DefaultSAF =
            this.defaultSAF

}
