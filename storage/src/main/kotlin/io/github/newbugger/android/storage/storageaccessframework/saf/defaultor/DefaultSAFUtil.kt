package io.github.newbugger.android.storage.storageaccessframework.saf.defaultor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.saf.SAFUtil
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAF.Companion.defaultSAF


@RequiresApi(26)
object DefaultSAFUtil {

    fun getDefaultSAFUriRecord(context: Context, appName: String, uri: Uri) {
        context.defaultSAF().put(appName, uri)
    }

    fun takePersistableUriPermission(context: Context, appName: String, uri: Uri) {
        if (SAFUtil.takePersistableUriPermission(context, uri)) {
            getDefaultSAFUriRecord(context, appName, uri)
        }
    }

    fun intentActionOpenDocumentTree(table: String = Environment.DIRECTORY_DOWNLOADS): Intent {
        return SAFUtil.intentActionOpenDocumentTree(table)
    }

    private fun Context.defaultSAF(): DefaultSAF =
            this.defaultSAF

}
