package io.github.newbugger.android.storage.storageaccessframework.defaultor

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.DocumentFileUtil
import io.github.newbugger.android.storage.storageaccessframework.defaultor.DefaultSAF.Companion.defaultSAF
import java.io.FileNotFoundException
import java.io.IOException


@RequiresApi(26)
object DefaultDocumentFileUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun listFiles(context: Context, appName: String, appNameDefault: String): Map<String, Uri> {
        val map = hashMapOf<String, Uri>()
        (DocumentFileUtil.listFiles(context, context.appNameUri(appName, appNameDefault)) ?: throw FileNotFoundException()).forEach {
            map[it.name!!] = it.uri
        }
        return map
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getFile(context: Context, appName: String, appNameDefault: String, displayName: String): Uri =
            DocumentFileUtil.findFile(context, context.appNameUri(appName, appNameDefault), displayName).uri

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun newFile(context: Context, appName: String, appNameDefault: String, displayName: String, mimeType: String, override: Boolean): Uri =
            DocumentFileUtil.newFile(context, context.appNameUri(appName, appNameDefault), displayName, mimeType, override).uri

    private fun Context.appNameUri(appName: String, appNameDefault: String): Uri {
        return if (this.defaultSAF().check(appName)) {
            this.defaultSAF().getUri(appName)
        } else {
            DocumentFileUtil.newDirectory(this, this.defaultUri(appNameDefault), appName).uri.also {
                this.defaultSAF().put(appName, it.toString())
            }
        }
    }

    // root path Blocker/
    private fun Context.defaultUri(appNameDefault: String): Uri =
            this.defaultSAF().getUri(appNameDefault)

    private fun Context.defaultSAF(): DefaultSAF =
            this.defaultSAF

}
