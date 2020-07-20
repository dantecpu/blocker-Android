package io.github.newbugger.android.libkit.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import io.github.newbugger.android.libkit.libsu.LibsuCommand
import java.io.File


object FileUtils {

    @JvmStatic
    fun isExist(path: String): Boolean {
        return try {
            val output = LibsuCommand.output(LibsuCommand.command("[ -f '$path' ] && echo \"yes\" || echo \"no\"")).joinToString(separator = ", ")
            when (output.trim()) {
                "yes" -> true
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun listFiles(path: String): List<String> {
        val output = LibsuCommand.output(LibsuCommand.command("find '$path'"))
        if (output.joinToString(separator = ", ").contains("No such file or directory")) {
            return ArrayList()
        }
        return output.filterNot { it.isEmpty() || it == path }
    }

    @JvmStatic
    fun count(path: String): Int {
        return File(path).let { f ->
            f.walkTopDown().filterNot { it == f }.count()
        }
    }

    @JvmStatic
    fun chmod(path: String, permission: Int, recursively: Boolean) {
        val comm = when (recursively) {
            true -> "chmod $permission '$path'"
            false -> "chmod -R $permission '$path'"
        }
        LibsuCommand.command(comm)
    }

    @JvmStatic
    fun read(path: String): String {
        val comm = "cat '$path'"
        if (!isExist(path)) {
            return ""
        }
        return LibsuCommand.output(LibsuCommand.command(comm)).joinToString(separator = ", ")
    }

    @JvmStatic
    fun cat(source: String, dest: String) {
        val comm = "cat '$source' > '$dest'"
        LibsuCommand.command(comm)
    }

    /*@JvmStatic
    fun copy(source: String, dest: String): Boolean {
    Log.d(tag, "Copy $source to $dest")
    try {
        FileInputStream(source).use { input ->
            FileOutputStream(dest).use { output ->
                val buf = ByteArray(1024)
                var length = input.read(buf)
                while (length > 0) {
                    output.write(buf, 0, length)
                    length = input.read(buf)
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e(tag, e.message)
        return false
    }
    return true
    }*/

    /*@JvmStatic
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }*/

    /*@JvmStatic
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }*/

    @JvmStatic
    fun getExternalStoragePath(context: Context): String {
        return if (Build.VERSION.SDK_INT > 28) {
            context.getExternalFilesDir(null)!!.absolutePath
        } else {
            Environment.getExternalStorageDirectory().absolutePath
        }
    }

    // api 29 only, a dirty usage
    @RequiresApi(29)
    @JvmStatic
    fun getExternalStoragePath(): String {
        return "/storage/emulated/0/Blocker"
    }

    // api 29 only, a dirty usage
    @RequiresApi(29)
    @JvmStatic
    fun getExternalStorageMove(src: String, dst: String) {
        copy(src, dst)
    }

    @JvmStatic
    fun copy(source: String, dest: String) {
        val comm = "cp -RTf $source $dest"
        LibsuCommand.command(comm)
    }

    @JvmStatic
    fun delete(file: String, recursively: Boolean): Boolean {
        val comm = if (recursively) {
            "rm -rf '$file'"
        } else {
            "rm -f '$file'"
        }
        val output = LibsuCommand.output(LibsuCommand.command(comm)).joinToString(separator = ", ")
        return output.trim().isEmpty()
    }

    /*@JvmStatic
    fun getFileName(path: String): String {
        val trimmedPath = path.trim()
        if (trimmedPath.isEmpty()) {
            return ""
        }
        val filename = trimmedPath.split("/").last()
        val extensionDotPosition = filename.lastIndexOf(".")
        return if (extensionDotPosition <= 0) {
            ""
        } else {
            filename.substring(0, extensionDotPosition)
        }
    }*/

    @JvmStatic
    fun getFileCounts(pathString: String, filter: String): Int {
        val path = File(pathString)
        if (!path.exists()) {
            return 0
        }
        if (path.isFile) {
            return 1
        }
        return path.walkTopDown().filter { it.name.contains(filter) }.count()
    }

    // From aFileChooser, https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
    @JvmStatic
    fun getUriPath(context: Context, uri: Uri?): String? {
        // DocumentProvider
        if (uri == null) {
            return null
        }
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return context.getExternalFilesDir(null).toString() +
                            File.separator + "blocker" +
                            File.separator + split[1]
                }
                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)
        return null
    }

    @JvmStatic
    fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        if (uri != null) {
            try {
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        return null
    }

    @JvmStatic
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    @JvmStatic
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    @JvmStatic
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    @JvmStatic
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

}
