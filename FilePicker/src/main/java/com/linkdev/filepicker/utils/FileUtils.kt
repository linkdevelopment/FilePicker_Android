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

package com.linkdev.filepicker.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


internal object FileUtils {
    const val TAG = "FilePickerTag"
    const val CAMERA_IMAGE_TYPE = ".jpg"
    const val CAMERA_VIDEO_TYPE = ".mp4"
    const val IMAG_PREFIX = "IMG_"
    const val VID_PREFIX = "VID_"
    private const val GENERAL_PREFIX = "FILE_"
    private const val BUFFER_SIZE = 4096

    // get file extension
    fun getExtensionFromUri(context: Context?, uri: Uri?): String? {
        val mimeType: String?
        mimeType = if (uri?.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context?.contentResolver?.getType(uri))
        } else
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())

        return mimeType
    }

    // get file mimeType
    fun getFileMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        }
    }

    // return file name with extension
    fun getFullFileNameFromUri(context: Context, uri: Uri): String {
        return "${getFileNameFromUri(context, uri)}.${getExtensionFromUri(context, uri)}"
    }

    // get file name
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val name = cursor.getString(nameIndex)
                if (name.lastIndexOf('.') != -1)
                    name.substring(0, name.lastIndexOf('.'))
                else
                    name.substring(0)
            } else {
                getUniqueFileName(GENERAL_PREFIX)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getUniqueFileName(GENERAL_PREFIX)
        } finally {
            cursor?.close()
        }
    }

    fun getFileFromPath(filePath: String?): File? {
        return filePath?.let { File(it) }
    }

    @Throws(Exception::class)
    fun copyStream(inputStream: InputStream, outputStream: FileOutputStream) {
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
    }

    // Retrieves String file path from a document schemed Uri
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val file: File?
        try {
            file = File.createTempFile(
                getFileNameFromUri(context, uri),
                "." + getExtensionFromUri(context, uri), context.externalCacheDir
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

    // create image file
    fun createImageFile(context: Context): File? {
        try {
            val uniqueFileName = getUniqueFileName(IMAG_PREFIX)
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
            return File.createTempFile(uniqueFileName, CAMERA_IMAGE_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // create video file
    fun createVideoFile(context: Context): File? {
        try {
            val uniqueFileName = getUniqueFileName(VID_PREFIX)
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
            return File.createTempFile(uniqueFileName, CAMERA_VIDEO_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Write file
    fun writeMedia(context: Context, uri: Uri, fileName: String, folderName: String): File? {
        return try {
            val file = createAppFile(fileName, folderName)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val outputStream = FileOutputStream(file)
                copyStream(inputStream, outputStream)
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
                copyStream(inputStream, outputStream)
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
        val folder = File(
            Environment.getExternalStorageDirectory().absolutePath
                    + "/"
                    + folderName
        )
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

    fun getUniqueFileName(prefix: String): String =
        prefix + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())

    fun getUniqueFileNameWithExt(prefix: String, extension: String): String =
        getUniqueFileName(prefix) + extension

    fun getPublicStorageDirPath(directory: String): String {
        return Environment.getExternalStoragePublicDirectory(directory)
            .absolutePath
    }

    fun getFileUri(context: Context, fileUrl: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, Constants.PROVIDER_AUTH, File(fileUrl))
        } else {
            Uri.fromFile(File(fileUrl))
        }
    }

    // add Image to gallery
    fun addMediaToGallery(file: File?, context: Context) {
        file?.let {
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(it.path)
                mediaScanIntent.data = Uri.fromFile(f)
                context.sendBroadcast(mediaScanIntent)
            }
        }
    }
}