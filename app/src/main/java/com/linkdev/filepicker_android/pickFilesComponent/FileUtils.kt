package com.linkdev.filepicker_android.pickFilesComponent

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.webkit.MimeTypeMap
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.SyncStateContract
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object FileUtils {
    // get file extension
    fun getExtensionFromUri(context: Context?, uri: Uri?): String? {
        var mimeType: String? = ""

        mimeType = if (uri?.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context?.contentResolver?.getType(uri))
        } else
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())

        return mimeType
    }

    // get file name
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        try {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name.substring(0, name.lastIndexOf('.'))
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun getFileFromPath(filePath: String?): File? {
        return if (filePath != null) File(filePath) else return null
    }

    private fun copyStream(inputStream: InputStream, outputStream: FileOutputStream) {
        val BUFFER_SIZE = 4096
        try {
            val bytes = ByteArray(BUFFER_SIZE)
            var count = 0
            while (count != -1) {
                count = inputStream.read(bytes)
                if (count != -1) {
                    outputStream.write(bytes, 0, count)
                }
            }
            outputStream.flush()
            inputStream.close()
            outputStream.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Retrieves String file path from a document schemed Uri using fileDescriptor.
     */
    fun getFilePathFromDocument(context: Context, uri: Uri): String? {
        val file: File?
        try {
            file = File.createTempFile(
                BuildConfig.APPLICATION_ID + getFileNameFromUri(context, uri),
                "." + getExtensionFromUri(context, uri), context.cacheDir
            )

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null && file != null) {
                val outputStream = FileOutputStream(file)
                copyStream(inputStream, outputStream)
            } else {
                return null
            }
            file.deleteOnExit()
            return file.path
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}