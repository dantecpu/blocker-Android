/*
 * Copyright (c) 2018-2020 : NewBugger (https://github.com/NewBugger)
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */

package io.github.newbugger.android.storage.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


/**
 * from https://github.com/topjohnwu/Magisk/commit/9e81db8
 */

object MediaStoreUtil {

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreDisplayName(uri: Uri): String {
        require(uri.scheme == "content") { "Uri lacks 'content' scheme: $uri" }
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val displayNameColumn = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                return cursor.getString(displayNameColumn)
            }
        }
        return uri.toString()
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreInputStream(uri: Uri) =
        contentResolver.openInputStream(uri)?.buffered() ?: throw FileNotFoundException()

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun Context.defaultMediaStoreOutputStream(uri: Uri) =
        contentResolver.openOutputStream(uri)?.buffered() ?: throw FileNotFoundException()

    @RequiresApi(29)
    object Images {
        fun getFolderFile(context: Context, appName: String, mimeType: String? = null): MutableList<MediaStoreFile?> {
            // context.requestStorageReadPermission()
            return context.getFolderFile(tableUri, relativePath(appName), mimeType)
        }

        fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): MediaStoreFile? {
            return context.getFile(tableUri, relativePath(appName), displayName, mimeType)
        }

        fun newFile(context: Context, appName: String, displayName: String, mimeType: String? = null): MediaStoreFile {
            return context.newFile(tableUri, relativePath(appName), displayName, mimeType)
        }

        private fun relativePath(appName: String): String = Environment.DIRECTORY_PICTURES + File.separator + appName

        private val tableUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    /**
     * https://developer.android.com/training/data-storage/shared/media#app-attribution
     *
     * if your app creates a media file that's stored in the photos, videos, or audio files media collection,
     * your app has access to the file.
     *
     * if your app wants to access a file within the MediaStore.Downloads collection that your app didn't create,
     * you must use the Storage Access Framework.
     *
     * Need the READ_EXTERNAL_STORAGE permission if accessing video files that your app didn't create.
     */
    @RequiresApi(29)
    object Downloads {
        fun getFolderFile(context: Context, appName: String, mimeType: String? = null): MutableList<MediaStoreFile?> {
            // context.requestStorageReadPermission()
            return context.getFolderFile(tableUri, relativePath(appName), mimeType)
        }

        fun getFile(context: Context, appName: String, displayName: String, mimeType: String? = null): MediaStoreFile? {
            // context.requestStorageReadPermission()
            return context.getFile(tableUri, relativePath(appName), displayName, mimeType)
        }

        fun newFile(context: Context, appName: String, displayName: String, mimeType: String? = null): MediaStoreFile {
            // context.requestStorageReadPermission()
            return context.newFile(tableUri, relativePath(appName), displayName, mimeType)
        }

        private fun relativePath(appName: String): String = Environment.DIRECTORY_DOWNLOADS + File.separator + appName

        private val tableUri: Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    }

    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.getFolderFile(tableUri: Uri, relativePath: String, mimeType: String? = null): MutableList<MediaStoreFile?> {
        return queryFolderFile(tableUri, relativePath, mimeType)
    }

    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.getFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String? = null): MediaStoreFile? {
        return queryFile(tableUri, relativePath, displayName, mimeType)
    }

    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.newFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String? = null): MediaStoreFile {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insertFile(tableUri, relativePath, displayName, mimeType)
        } else {
            queryFile(tableUri, relativePath, displayName, mimeType)?.delete()
            insertFile(tableUri, relativePath, displayName, mimeType)
        }
    }

    /**
     * todo bug: cannot use MediaStore.MediaColumns.RELATIVE_PATH on either query.selection or cursor.getColumn
     * so use deprecated MediaStore.MediaColumns.DATA
     */
    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.queryFolderFile(tableUri: Uri, relativePath: String, mimeType: String? = null): MutableList<MediaStoreFile?> {
        val collection: MutableList<MediaStoreFile?> = ArrayList()

        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.RELATIVE_PATH, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.MIME_TYPE)
        val sortOrder = "${MediaStore.MediaColumns.DISPLAY_NAME} ASC"

        contentResolver.query(tableUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val relativeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val data = cursor.getString(dataColumn)
                val relative = cursor.getString(relativeColumn)
                val name = cursor.getString(nameColumn)
                val mime = cursor.getString(mimeColumn)
                val relativeName = relativePath + File.separator + name
                if ((data.endsWith(relativeName) || relative == relativePath) && (mimeType == null || mimeType == mime)) {
                    collection.add(MediaStoreFile(id, relativeName, tableUri, contentResolver))
                }
            }
        }

        return collection
    }

    /**
     * todo bug: cannot use MediaStore.MediaColumns.RELATIVE_PATH on either query.selection or cursor.getColumn
     * so use deprecated MediaStore.MediaColumns.DATA
     */
    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.queryFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String? = null): MediaStoreFile? {
        val relativeName = relativePath + File.separator + displayName

        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.RELATIVE_PATH, MediaStore.MediaColumns.MIME_TYPE)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} == ?"
        val selectionArgs = arrayOf(displayName)
        val sortOrder = "${MediaStore.MediaColumns.DISPLAY_NAME} ASC"

        contentResolver.query(tableUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val relativeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val data = cursor.getString(dataColumn)
                val relative = cursor.getString(relativeColumn)
                val mime = cursor.getString(mimeColumn)
                if ((data.endsWith(relativeName) || relative == relativePath) && (mimeType == null || mimeType == mime)) {
                    return MediaStoreFile(id, relativeName, tableUri, contentResolver)
                }
            }
        }

        return null
    }

    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.insertFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String? = null): MediaStoreFile {
        val fileUri = insertValue(tableUri, relativePath, displayName, mimeType)

        val projection = arrayOf(MediaStore.MediaColumns._ID)

        contentResolver.query(fileUri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idColumn)
                return MediaStoreFile(id, relativePath + File.separator + displayName, tableUri, contentResolver)
            }
        }

        throw IOException("can not insertFile $relativePath/$displayName")
    }

    @RequiresApi(29)
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.insertValue(tableUri: Uri, relativePath: String, displayName: String, mimeType: String? = null): Uri {
        return ContentValues(3).apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            if (mimeType != null) put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        }.let {
            // before Android 11, MediaStore can not rename new file when file exists,
            // insert will return null. use newFile() instead.
            contentResolver.insert(tableUri, it)
        } ?: throw IOException("can not insertValue $relativePath/$displayName")
    }

    data class MediaStoreFile(private val id: Long,
                              private val data: String,
                              private val tableUri: Uri,
                              private val contentResolver: ContentResolver) {
        val uri: Uri = ContentUris.withAppendedId(tableUri, id)
        val relativeName: String = data

        @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
        fun delete(): Boolean {
            val selection = "${MediaStore.MediaColumns._ID} == ?"
            val selectionArgs = arrayOf(id.toString())
            return contentResolver.delete(uri, selection, selectionArgs) == 1
        }
    }

}
