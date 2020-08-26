package io.github.newbugger.android.storage.storageaccessframework.entity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.SAFUtil
import io.github.newbugger.android.storage.storageaccessframework.entity.DefaultSAF.Companion.defaultSAF


@RequiresApi(26)
object DefaultSAFUtil {

    fun checkDefaultSAFUriPermission(context: Context): Boolean {
        return context.defaultSAF().check()
    }

    fun takePersistableUriPermission(context: Context, uri: Uri) {
        if (SAFUtil.takePersistableUriPermission(context, uri)) {
            context.defaultSAF().put(uri)
        }
    }

    fun intentActionOpenDocumentTree(table: String = Environment.DIRECTORY_DOWNLOADS): Intent {
        return SAFUtil.intentActionOpenDocumentTree(table)
    }

    private fun Context.defaultSAF() =
            this.defaultSAF

}
