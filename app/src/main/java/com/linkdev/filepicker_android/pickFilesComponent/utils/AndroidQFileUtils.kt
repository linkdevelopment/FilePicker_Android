package com.linkdev.filepicker_android.pickFilesComponent.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.models.MimeType


object AndroidQFileUtils {
    fun getPhotoUri(
        context: Context, prefix: String, mimeType: MimeType, shouldMakeDir: Boolean
    ): Uri? {
        val relativePath: String = if (shouldMakeDir) {
            Environment.DIRECTORY_PICTURES + "/" + context.getString(R.string.app_name) + "/"
        } else {
            Environment.DIRECTORY_PICTURES
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
        saveFile(context, uriSavedPhoto)
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        uriSavedPhoto?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
        return uriSavedPhoto
    }

    // broadcast image to gallery
    private fun saveFile(context: Context, uri: Uri?) {
        try {
            val let = uri?.let { context.contentResolver.openOutputStream(it) }
            let?.flush()
            let?.close()
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    // delte file with given uri
    fun deleteUri(context: Context, uri: Uri?) {
        uri?.let { context.contentResolver.delete(it, null, null) }
    }

    fun getVideoUri(
        context: Context, prefix: String, mimeType: MimeType, shouldMakeDir: Boolean
    ): Uri? {
        val relativePath: String = if (shouldMakeDir) {
            Environment.DIRECTORY_MOVIES + "/" + context.getString(R.string.app_name)
        } else {
            Environment.DIRECTORY_MOVIES
        }

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, FileUtils.getUniqueFileName(prefix))
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType.mimeTypeName)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val collection =
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriSavedPhoto = resolver.insert(collection, contentValues)
        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        uriSavedPhoto?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
        return uriSavedPhoto
    }
}