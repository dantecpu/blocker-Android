package io.github.newbugger.android.storage.mediastore

import android.content.Context
import io.github.newbugger.android.storage.mediastore.DefaultMediaStore.Companion.defaultMediaStore


object MediaStoreUtil {

    object Images {
        fun getFolderFile(context: Context, appName: String, mimeType: String? = null): MutableList<DefaultMediaStore.MediaStoreFile?> {
            return context.defaultMediaStore().Images().getFolderFile(appName, mimeType)
        }

        fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): DefaultMediaStore.MediaStoreFile? {
            return context.defaultMediaStore().Images().getFile(appName, displayName, mimeType)
        }

        fun newFile(context: Context, appName: String, displayName: String, mimeType: String? = null, override: Boolean = false): DefaultMediaStore.MediaStoreFile {
            return context.defaultMediaStore().Images().newFile(appName, displayName, mimeType, override)
        }
    }

    object Downloads {
        fun getFolderFile(context: Context, appName: String, mimeType: String? = null): MutableList<DefaultMediaStore.MediaStoreFile?> {
            return context.defaultMediaStore().Downloads().getFolderFile(appName, mimeType)
        }

        fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): DefaultMediaStore.MediaStoreFile? {
            return context.defaultMediaStore().Downloads().getFile(appName, displayName, mimeType)
        }

        fun newFile(context: Context, appName: String, displayName: String, mimeType: String? = null, override: Boolean = false): DefaultMediaStore.MediaStoreFile {
            return context.defaultMediaStore().Downloads().newFile(appName, displayName, mimeType, override)
        }
    }

    private fun Context.defaultMediaStore(): DefaultMediaStore =
            this.defaultMediaStore

}
