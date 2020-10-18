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
    /**
     * Copy image to public external storage and scan to gallery
     * @param context caller caller activity/fragment context
     * @param uri image URI
     * @param currentCapturedPath image file path in external storage
     * @param folderName desired gallery folder name
     * */
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
            writePublicFile(context, Environment.DIRECTORY_PICTURES, uri, fileNameWithExt)
        }
        file?.let { broadcastFile(it, context) }
    }

    /**
     * Copy video to public external storage and scan to gallery
     * @param context caller caller activity/fragment context
     * @param uri video URI
     * @param currentCapturedPath video file path in external
     * @param folderName desired gallery folder name
     * */
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
            writePublicFile(context, Environment.DIRECTORY_MOVIES, uri, fileNameWithExt)
        }
        file?.let { broadcastFile(file, context) }
    }

    /** scan given file from public storage to gallery app
     * @param file file to be scanned
     * @param context caller activity/fragment context
     * */
    private fun broadcastFile(file: File?, context: Context) {
        file?.let { MediaScannerConnection.scanFile(context, arrayOf(it.path), null, null) }
    }

    /**
     * create file in public external storage in given directory [folderName]
     * @param context caller activity/fragment context
     * @param uri file uri
     * @param fileName desired file name
     * @param folderName app specific folder name
     * */
    private fun writeMedia(
        context: Context, uri: Uri, fileName: String, folderName: String
    ): File? {
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

    /**
     * create file in public external storage in given public directory e.g [Environment.DIRECTORY_PICTURES]
     * @param context caller activity/fragment context
     * @param directory a public directory
     * @param uri file uri
     * @param fileName desired file name
     * */
    private fun writePublicFile(
        context: Context, directory: String, uri: Uri, fileName: String
    ): File? {
        return try {
            val filePath = getPublicStorageDirPath(directory) + "/" + fileName
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

    private fun createAppFile(fileName: String?, folderName: String): File =
        File(getFilePath(fileName, folderName))


    private fun makeDirectory(folderName: String): String {
        val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + folderName)
        if (!folder.exists())
            folder.mkdirs()
        return folder.path
    }

    private fun getFilePath(fileName: String?, folderName: String) =
        makeDirectory(folderName) + "/" + fileName

    private fun getUniqueFileNameWithExt(prefix: String, extension: String): String =
        FileUtils.getUniqueFileName(prefix) + extension

    /**
     * Returns absolute path for external storage with given public directory
     * @param directory desired public directory e.g [Environment.DIRECTORY_PICTURES]
     * */
    private fun getPublicStorageDirPath(directory: String): String {
        return Environment.getExternalStoragePublicDirectory(directory).absolutePath
    }
}