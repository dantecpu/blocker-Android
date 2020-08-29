package io.github.newbugger.android.blocker.util.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAFUtil


@RequiresApi(26)
object SAFLocalUtil {

    fun getDefaultSAFUriRecord(context: Context, appName: String, uri: Uri) {
        DefaultSAFUtil.getDefaultSAFUriRecord(context, appName, uri)
    }

    fun takePersistableUriPermission(context: Context, appName: String, uri: Uri) {
        DefaultSAFUtil.takePersistableUriPermission(context, appName, uri)
    }

    fun intentActionOpenDocumentTree(): Intent {
        return DefaultSAFUtil.intentActionOpenDocumentTree(Environment.DIRECTORY_DOWNLOADS)
    }

}
