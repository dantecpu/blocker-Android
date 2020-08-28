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
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


/**
 * from https://github.com/topjohnwu/Magisk/commit/9e81db8
 */

@RequiresApi(30)
class DefaultMediaStore(private val context: Context) {

    inner class Images {
        fun getFolderFile(appName: String, mimeType: String? = null, owner: String? = null): MutableList<MediaStoreFile?> {
            return context.getFolderFile(tableUri, relativePath(appName), mimeType, owner)
        }

        fun getFile(appName: String, displayName: String, mimeType: String? = null): MediaStoreFile? {
            return context.getFile(tableUri, relativePath(appName), displayName, mimeType)
        }

        fun newFile(appName: String, displayName: String, mimeType: String? = null, override: Boolean = false): MediaStoreFile {
            return context.newFile(tableUri, relativePath(appName), displayName, mimeType, override)
        }

        private fun relativePath(appName: String): String = Environment.DIRECTORY_PICTURES + File.separator + appName + File.separator

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
    inner class Downloads {
        fun getFolderFile(appName: String, mimeType: String? = null, owner: String? = null): MutableList<MediaStoreFile?> {
            return context.getFolderFile(tableUri, relativePath(appName), mimeType, owner)
        }

        fun getFile(appName: String, displayName: String, mimeType: String? = null): MediaStoreFile? {
            return context.getFile(tableUri, relativePath(appName), displayName, mimeType)
        }

        fun newFile(appName: String, displayName: String, mimeType: String? = null, override: Boolean = false): MediaStoreFile {
            return context.newFile(tableUri, relativePath(appName), displayName, mimeType, override)
        }

        private fun relativePath(appName: String): String = Environment.DIRECTORY_DOWNLOADS + File.separator + appName + File.separator

        private val tableUri: Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.getFolderFile(tableUri: Uri, relativePath: String, mimeType: String?, owner: String?): MutableList<MediaStoreFile?> {
        return queryFolderFile(tableUri, relativePath, mimeType, owner)
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.getFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String?): MediaStoreFile? {
        return queryFile(tableUri, relativePath, displayName, mimeType)
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.newFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String?, override: Boolean): MediaStoreFile {
        if (override) queryFile(tableUri, relativePath, displayName, mimeType)?.delete()
        return insertFile(tableUri, relativePath, displayName)
    }

    /**
     * bug: cannot read MediaStore.MediaColumns.RELATIVE_PATH on Android 10
     * but available on Android 11
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.queryFolderFile(tableUri: Uri, relativePath: String, mimeType: String?, owner: String?): MutableList<MediaStoreFile?> {
        val collection = mutableListOf<MediaStoreFile?>()

        val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.OWNER_PACKAGE_NAME
        )
        val selection = null
        val selectionArgs = null
        val sortOrder = "${MediaStore.MediaColumns.RELATIVE_PATH} ASC"

        contentResolver.query(tableUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val relativeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val ownerColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.OWNER_PACKAGE_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mime = cursor.getString(mimeColumn)
                val relative = cursor.getString(relativeColumn)
                val name = cursor.getString(nameColumn)
                val ownerC = cursor.getString(ownerColumn)
                val relativeName = relativePath + name
                if (relative == relativePath && (mimeType == null || mimeType == mime) && (owner == null || owner == ownerC)) {
                    collection.add(MediaStoreFile(id, relativeName, tableUri, contentResolver))
                }
            }
        }

        return collection
    }

    /**
     * bug: cannot read MediaStore.MediaColumns.RELATIVE_PATH on Android 10
     * but available on Android 11
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.queryFile(tableUri: Uri, relativePath: String, displayName: String, mimeType: String?): MediaStoreFile? {

        val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH
        )
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} == ?"
        val selectionArgs = arrayOf(displayName)
        val sortOrder = "${MediaStore.MediaColumns.DISPLAY_NAME} ASC"

        contentResolver.query(tableUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val relativeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mime = cursor.getString(mimeColumn)
                val relative = cursor.getString(relativeColumn)
                val relativeName = relativePath + displayName
                if (relative == relativePath && (mimeType == null || mimeType == mime)) {
                    return MediaStoreFile(id, relativeName, tableUri, contentResolver)
                }
            }
        }

        return null
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.insertFile(tableUri: Uri, relativePath: String, displayName: String): MediaStoreFile {
        val fileUri = insertValue(tableUri, relativePath, displayName)

        val projection = arrayOf(MediaStore.MediaColumns._ID)

        contentResolver.query(fileUri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idColumn)
                val relativeName = relativePath + displayName
                return MediaStoreFile(id, relativeName, tableUri, contentResolver)
            }
        }

        throw IOException("can not insertFile $relativePath$displayName")
    }

    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    private fun Context.insertValue(tableUri: Uri, relativePath: String, displayName: String): Uri {
        return ContentValues(2).apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        }.let {
            contentResolver.insert(tableUri, it)
        } ?: throw IOException("can not insertValue $relativePath$displayName")
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

    companion object {
        val Context.defaultMediaStore: DefaultMediaStore
            get() = DefaultMediaStore(this)
    }

}
