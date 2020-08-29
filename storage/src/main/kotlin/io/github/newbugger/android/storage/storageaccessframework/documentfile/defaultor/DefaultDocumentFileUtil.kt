package io.github.newbugger.android.storage.storageaccessframework.documentfile.defaultor

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAF
import io.github.newbugger.android.storage.storageaccessframework.documentfile.DocumentFileUtil
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAF.Companion.defaultSAF
import io.github.newbugger.android.storage.storageaccessframework.saf.defaultor.DefaultSAFUnavailableException
import java.io.FileNotFoundException
import java.io.IOException


@RequiresApi(26)
object DefaultDocumentFileUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun listFiles(context: Context, appName: String, mimeType: String? = null): MutableMap<String?, Uri?> {
        val map = mutableMapOf<String?, Uri?>()
        DocumentFileUtil.listFiles(context, context.defaultUri(appName)).filter {
            it?.isFile == true && (mimeType == null || mimeType == it.type)
        }.forEach {
            map[it?.name] = it?.uri
        }
        return map
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun getFile(context: Context, appName: String, displayName: String, extension: String? = null, mimeType: String? = null): Uri =
            DocumentFileUtil.findFile(context, context.defaultUri(appName), displayName, extension, mimeType)?.uri ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun newFile(context: Context, appName: String, displayName: String, extension: String? = null, mimeType: String, override: Boolean): Uri =
            DocumentFileUtil.newFile(context, context.defaultUri(appName), displayName, extension, mimeType, override).uri

    /**
     * but DocumentFile class uses DocumentUri: if given Uri is backed by a DocumentsProvider
     * so use DocumentFile to create and get Uri is not achievable
     */
    private fun Context.appNameUri(appName: String, appNameDefault: String): Uri {
        return if (this.defaultSAF().check(appName)) {
            this.defaultUri(appName)
        } else {
            DocumentFileUtil.newDirectory(this, this.defaultUri(appNameDefault), appName).uri.also {
                this.defaultSAF().put(appName, it)
            }
        }
    }

    private fun Context.defaultUri(appName: String): Uri =
            this.defaultSAF().getUri(appName) ?: throw DefaultSAFUnavailableException()

    private fun Context.defaultSAF(): DefaultSAF =
            this.defaultSAF

}
