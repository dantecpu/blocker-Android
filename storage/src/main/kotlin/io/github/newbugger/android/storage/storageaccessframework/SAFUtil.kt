/*
 * Copyright (c) 2018-2020 : NewBugger (https://github.com/NewBugger)
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.newbugger.android.storage.storageaccessframework

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.annotation.RequiresApi
import io.github.newbugger.android.storage.BuildConfig


@RequiresApi(26)
object SAFUtil {

    // todo: reduce the uri permission level ?
    fun takePersistableUriPermission(context: Context, uri: Uri): Boolean {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        return (checkUriPermission(context, uri)).also {
            if (BuildConfig.DEBUG) Log.e(javaClass.name, it.toString())
        }
    }

    fun intentActionOpenDocumentTree(table: String = Environment.DIRECTORY_DOWNLOADS): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, table)
        }
    }

    // https://stackoverflow.com/q/6307793
    private fun checkUriPermission(context: Context, uri: Uri): Boolean {
        return context.checkUriPermission(
                uri,
                Binder.getCallingPid(),
                Binder.getCallingUid(),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

}
