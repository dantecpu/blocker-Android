package io.github.newbugger.android.storage.storageaccessframework.defaultor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.SAFUtil
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultSAF.Companion.defaultSAF


@RequiresApi(26)
object DefaultSAFUtil {

    fun checkDefaultSAFUriPermission(context: Context, appName: String): Boolean {
        return context.defaultSAF().check(appName)
    }

    fun takePersistableUriPermission(context: Context, appName: String, uri: Uri) {
        if (SAFUtil.takePersistableUriPermission(context, uri)) {
            context.defaultSAF().put(appName, uri.toString())
        }
    }

    fun intentActionOpenDocumentTree(table: String = Environment.DIRECTORY_DOWNLOADS): Intent {
        return SAFUtil.intentActionOpenDocumentTree(table)
    }

    private fun Context.defaultSAF(): DefaultSAF =
            this.defaultSAF

}
