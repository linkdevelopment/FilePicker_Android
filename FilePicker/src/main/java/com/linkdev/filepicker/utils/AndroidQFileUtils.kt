package com.linkdev.filepicker.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.linkdev.filepicker.models.MimeType


object AndroidQFileUtils {
    fun getPhotoUri(
        context: Context, prefix: String, mimeType: MimeType, folderName: String?
    ): Uri? {
        val relativePath: String = if (folderName.isNullOrBlank()) {
            Environment.DIRECTORY_PICTURES
        } else {
            Environment.DIRECTORY_PICTURES + "/" + folderName
        }
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, FileUtils.getUniqueFileName(prefix))
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType.mimeTypeName)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val collection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriSavedPhoto = resolver.insert(collection, contentValues)
        broadcastFile(context, uriSavedPhoto)
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        uriSavedPhoto?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
        return uriSavedPhoto
    }

    fun getVideoUri(
        context: Context, prefix: String, mimeType: MimeType, folderName: String?
    ): Uri? {
        val relativePath: String = if (folderName.isNullOrBlank()) {
            Environment.DIRECTORY_MOVIES
        } else {
            Environment.DIRECTORY_MOVIES + "/" + folderName
        }

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, FileUtils.getUniqueFileName(prefix))
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType.mimeTypeName)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val collection =
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriSavedVideo = resolver.insert(collection, contentValues)
        broadcastFile(context, uriSavedVideo)
        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        uriSavedVideo?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
        return uriSavedVideo
    }

    // broadcast image to gallery
    private fun broadcastFile(context: Context, uri: Uri?) {
        try {
            val let = uri?.let { context.contentResolver.openOutputStream(it) }
            let?.flush()
            let?.close()
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    // delete file with given uri
    fun deleteUri(context: Context, uri: Uri?) {
        uri?.let { context.contentResolver.delete(it, null, null) }
    }

}