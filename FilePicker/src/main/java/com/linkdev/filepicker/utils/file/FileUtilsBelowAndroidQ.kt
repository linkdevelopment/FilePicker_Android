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

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtilsBelowAndroidQ {
    fun saveImageToGallery(
        context: Context, uri: Uri, currentCapturedPath: String?, folderName: String?
    ) {
        val file = if (folderName != null && currentCapturedPath != null) {
            val currentFile = File(currentCapturedPath)
            writeMedia(context, uri, currentFile.name, folderName)
        } else {
            val fileNameWithExt =
                getUniqueFileNameWithExt(
                    FileUtils.IMAG_PREFIX,
                    FileUtils.CAMERA_IMAGE_TYPE
                )
            writePublicFile(context, uri, fileNameWithExt)
        }
        file?.let { broadcastFile(it, context) }
    }

    fun saveVideoToGallery(
        context: Context, uri: Uri, currentCapturedPath: String?, folderName: String?
    ) {
        val file = if (folderName != null && currentCapturedPath != null) {
            val currentFile = File(currentCapturedPath)
            writeMedia(context, uri, currentFile.name, folderName)
        } else {
            val fileNameWithExt =
                getUniqueFileNameWithExt(
                    FileUtils.VID_PREFIX,
                    FileUtils.CAMERA_VIDEO_TYPE
                )
            writePublicFile(context, uri, fileNameWithExt)
        }
        file?.let { broadcastFile(file, context) }
    }

    private fun broadcastFile(file: File?, context: Context) {
        MediaScannerConnection.scanFile(context, arrayOf(file?.path), null, null)
    }

    // Write file
    fun writeMedia(context: Context, uri: Uri, fileName: String, folderName: String): File? {
        return try {
            val file = createAppFile(fileName, folderName)
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

    fun writePublicFile(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val filePath = getPublicStorageDirPath(Environment.DIRECTORY_PICTURES) + "/" + fileName
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

    // create external directory
    private fun makeDirectory(folderName: String): String {
        val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + folderName)
        if (!folder.exists())
            folder.mkdirs()
        return folder.path
    }

    // get file path in directory
    private fun getFilePath(fileName: String?, folderName: String) =
        makeDirectory(folderName) + "/" + fileName

    // get saved file in created path
    private fun createAppFile(fileName: String?, folderName: String): File =
        File(getFilePath(fileName, folderName))


    private fun getUniqueFileNameWithExt(prefix: String, extension: String): String =
        FileUtils.getUniqueFileName(prefix) + extension

    private fun getPublicStorageDirPath(directory: String): String {
        return Environment.getExternalStoragePublicDirectory(directory).absolutePath
    }
}