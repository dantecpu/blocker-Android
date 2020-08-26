package io.github.newbugger.android.blocker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.entity.DefaultSAFUtil


@RequiresApi(29)
object SAFLocalUtil {

    fun checkDefaultSAFUriPermission(context: Context): Boolean {
        return DefaultSAFUtil.checkDefaultSAFUriPermission(context)
    }

    fun takePersistableUriPermission(context: Context, uri: Uri) {
        DefaultSAFUtil.takePersistableUriPermission(context, uri)
    }

    fun intentActionOpenDocumentTree(): Intent {
        return DefaultSAFUtil.intentActionOpenDocumentTree(Environment.DIRECTORY_DOWNLOADS)
    }

}
