package com.linkdev.filepicker_android.pickFilesComponent.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object AndroidQFileUtils {
    fun getPhotoUri(context: Context, prefix: String, mimeType: MimeType): Uri? {
        val relativeLocation = Environment.DIRECTORY_PICTURES
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, FileUtils.getUniqueFileName(prefix))
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType.mimeTypeName)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 1)
        val resolver = context.contentResolver
        val collection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val uriSavedVideo = resolver.insert(collection, contentValues)
        Log.e("xxx", "uriSavedVideo $uriSavedVideo")
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        uriSavedVideo?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
        return uriSavedVideo
    }

    fun writePublicFile(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val filePath =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/" + fileName
            val file = File(filePath)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val outputStream = FileOutputStream(file)
                FileUtils.copyStream(inputStream, outputStream)
                file
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}