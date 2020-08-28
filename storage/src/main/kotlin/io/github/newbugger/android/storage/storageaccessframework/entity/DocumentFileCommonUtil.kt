package io.github.newbugger.android.storage.storageaccessframework.entity

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.DocumentFileUtil
import java.io.FileNotFoundException


@RequiresApi(26)
object DocumentFileCommonUtil {

    /**
     * get DocumentFile name
     */
    fun getFileName(context: Context, uri: Uri): String =
            DocumentFileUtil.getFile(context, uri).name ?: throw FileNotFoundException()

    fun getDirectoryName(context: Context, uri: Uri): String =
            DocumentFileUtil.getDirectory(context, uri).name ?: throw FileNotFoundException()

    /**
     * transfer Intent.getData().data Uri to DocumentUri
     */
    fun getFileUri(context: Context, uri: Uri): Uri =
            DocumentFileUtil.getFile(context, uri).uri

    fun getDirectoryUri(context: Context, uri: Uri): Uri =
            DocumentFileUtil.getDirectory(context, uri).uri

}
