/*
 * Copyright (C) 2020 Link Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkdev.filepicker.utils.file

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.linkdev.filepicker.models.MimeType
import java.io.File
import java.io.FileOutputStream


object AndroidQFileUtils {

    /** save image to gallery
     * @param context caller activity/fragment context
     * @param file desired file to be scanned
     * @param imageName file displayed name
     * @param folderName app specific gallery folder name
     * */
    internal fun saveImageToGallery(
        context: Context, file: File, imageName: String, folderName: String?
    ) {
        val relativePath: String = if (folderName.isNullOrBlank()) {
            Environment.DIRECTORY_PICTURES
        } else {
            Environment.DIRECTORY_PICTURES + "/" + folderName
        }
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, MimeType.JPEG.mimeTypeName)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val collection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val galleryUri = resolver.insert(collection, contentValues)
        broadcastFile(context, galleryUri, file)
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        galleryUri?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
    }

    /** save video to gallery
     * @param context caller activity/fragment context
     * @param file desired file to be scanned
     * @param videoName file displayed name
     * @param folderName app specific gallery folder name
     * */
    internal fun saveVideoToGallery(
        context: Context, file: File, videoName: String, folderName: String?
    ) {
        val relativePath: String = if (folderName.isNullOrBlank()) {
            Environment.DIRECTORY_MOVIES
        } else {
            Environment.DIRECTORY_MOVIES + "/" + folderName
        }

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, videoName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, MimeType.MP4.mimeTypeName)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = context.contentResolver
        val collection =
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriSavedVideo = resolver.insert(collection, contentValues)
        broadcastFile(context, uriSavedVideo, file)
        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        uriSavedVideo?.let {
            context.contentResolver.update(it, contentValues, null, null)
        }
    }

    /** scan given file from public storage to gallery app
     * @param context caller activity/fragment context
     * @param galleryUri saved uri after insert file values to content resolver
     * @param file file to be scanned
     * */
    private fun broadcastFile(context: Context, galleryUri: Uri?, file: File) {
        try {
            galleryUri?.let { uri ->
                context.contentResolver.openFileDescriptor(uri, "w", null).use { pfd ->
                    pfd?.let {
                        val output = FileOutputStream(it.fileDescriptor)
                        output.write(file.readBytes())
                        output.close()
                    }
                }
            }
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

}