package io.github.newbugger.android.storage.mediastore

import android.content.Context
import io.github.newbugger.android.storage.mediastore.MediaStoreClass.Companion.mediaStoreClass


object MediaStoreUtil {

    object Images {
        fun getFolderFile(context: Context, appName: String, mimeType: String? = null, owner: String? = null): MutableList<MediaStoreClass.MediaStoreFile?> {
            return context.mediaStoreClass().Images().getFolderFile(appName, mimeType, owner)
        }

        fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): MediaStoreClass.MediaStoreFile? {
            return context.mediaStoreClass().Images().getFile(appName, displayName, mimeType)
        }

        fun newFile(context: Context, appName: String, displayName: String, mimeType: String? = null, override: Boolean = false): MediaStoreClass.MediaStoreFile {
            return context.mediaStoreClass().Images().newFile(appName, displayName, mimeType, override)
        }
    }

    object Downloads {
        fun getFolderFile(context: Context, appName: String, mimeType: String? = null, owner: String? = null): MutableList<MediaStoreClass.MediaStoreFile?> {
            return context.mediaStoreClass().Downloads().getFolderFile(appName, mimeType, owner)
        }

        fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): MediaStoreClass.MediaStoreFile? {
            return context.mediaStoreClass().Downloads().getFile(appName, displayName, mimeType)
        }

        fun newFile(context: Context, appName: String, displayName: String, mimeType: String? = null, override: Boolean = false): MediaStoreClass.MediaStoreFile {
            return context.mediaStoreClass().Downloads().newFile(appName, displayName, mimeType, override)
        }
    }

    private fun Context.mediaStoreClass(): MediaStoreClass =
            this.mediaStoreClass

}
