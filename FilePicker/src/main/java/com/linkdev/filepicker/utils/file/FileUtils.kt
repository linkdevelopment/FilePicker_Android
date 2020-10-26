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

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Size
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.linkdev.filepicker.utils.ScalingUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*


internal object FileUtils {
    const val TAG = "FilePickerTag"
    const val CAMERA_IMAGE_TYPE = ".jpg"
    const val CAMERA_VIDEO_TYPE = ".mp4"
    const val IMAG_PREFIX = "IMG_"
    const val VID_PREFIX = "VID_"
    private const val DATE_PATTERN = "yyyyMMdd_HHmmss"
    private const val GENERAL_PREFIX = "FILE_"
    private const val BUFFER_SIZE = 4096

    /**
     * Return file extension
     * @param context caller activity/fragment context
     * @param uri file uri
     */
    fun getExtensionFromUri(context: Context?, uri: Uri?): String? {
        val mimeType: String?
        mimeType = if (uri?.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context?.contentResolver?.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        }
        return mimeType
    }

    /**
     * Returns file mime type
     * @param context caller activity/fragment context
     * @param uri file uri
     */
    fun getFileMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        }
    }

    /**
     * Return full file name with extension
     * @param context caller activity/fragment context
     * @param uri file uri
     */
    fun getFullFileNameFromUri(context: Context, uri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } else {
                getUniqueFileName(
                    GENERAL_PREFIX
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getUniqueFileName(
                GENERAL_PREFIX
            )
        } finally {
            cursor?.close()
        }
    }

    /**
     * Returns file name without extension
     * @param context caller activity/fragment context
     * @param uri file uri
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        val name = getFullFileNameFromUri(context, uri)
        return if (name.lastIndexOf('.') != -1)
            name.substring(0, name.lastIndexOf('.'))
        else
            name.substring(0)
    }

    /**
     * returns file size in bytes
     * @param context caller activity/fragment context
     * @param uri file uri
     * @return file size in bytes or -1 if uri is invalid
     */
    fun getFileSize(context: Context, uri: Uri): Double {
        val mCursor = context.contentResolver
            .query(uri, null, null, null, null, null)
        mCursor.use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                var size = "-1"
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex)
                }
                return size.toDouble()
            }
        }
        return -1.0
    }

    /** helper class copy stream
     * @param inputStream
     * @param outputStream
     */
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

    /**
     * create a temp file in cache dir in the cache subdirectory of your app's internal storage area
     * @param context caller activity/fragment context
     * @param uri file uri
     * @return file path from a document schemed Uri, the value returned by [android.content.Context.getCacheDir]
     * or null if uri is invalid*/
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val file: File?
        try {
            file = File.createTempFile(
                getFileNameFromUri(context, uri),
                "." + getExtensionFromUri(context, uri),
                context.cacheDir
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

    /**
     * Returns temp file created in the root of your app's external storage area
     * @param context caller activity/fragment context
     * @return Temp file the value returned by [android.content.Context.getExternalFilesDir]
     * in [Environment.DIRECTORY_PICTURES], or null
     * if some error occurred while creating a temp file.
     */
    fun createImageFile(context: Context): File? {
        return try {
            val uniqueFileName = getUniqueFileName(IMAG_PREFIX)
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(uniqueFileName, CAMERA_IMAGE_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns temp file created in the root of your app's external storage area
     * @param context caller activity/fragment context
     * @return Temp file the value returned by [android.content.Context.getExternalFilesDir]
     * in [Environment.DIRECTORY_MOVIES], or null if some error occurred while creating a temp file.
     */
    fun createVideoFile(context: Context): File? {
        return try {
            val uniqueFileName =
                getUniqueFileName(VID_PREFIX)
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            File.createTempFile(uniqueFileName, CAMERA_VIDEO_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * create a unique file name using given prefix and current date.
     * @param prefix file name prefix
     */
    fun getUniqueFileName(prefix: String): String =
        prefix + SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH).format(Date())

    /**
     * Returns file URI from file Path
     * @param context caller activity/fragment context
     * @param fileUrl file path in storage
     * @return uri using file provider if [Build.VERSION_CODES.N] or higher
     * or using [Uri.fromFile] for below versions
     * */
    fun getFileUri(context: Context, fileUrl: String): Uri {
        val packageName = context.applicationContext.packageName
        val authorities = "$packageName.filepicker.provider"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, authorities, File(fileUrl))
        } else {
            Uri.fromFile(File(fileUrl))
        }
    }

    /**
     * delete given uri if exist and valid
     * @param context caller activity/fragment context
     * @param uri saved uri
     * */
    fun deleteUri(context: Context, uri: Uri?) {
        try {
            uri?.let { context.contentResolver.delete(it, null, null) }
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
    }

    /**
     * create thumbnail for the given file path of image.
     * @param filePath image file path
     * @param thumbnailSize desired thumbnail size
     * @return resized bitmap as desired or null
     * */
    fun getImageThumbnail(filePath: String, thumbnailSize: Size): Bitmap? {
        return try {
            val decodedBitmap =
                ScalingUtils.decodeFile(filePath, thumbnailSize.width, thumbnailSize.height)
            val adjustedBitmap = getAdjustedBitmap(filePath, decodedBitmap)
            if (thumbnailSize.width == adjustedBitmap?.width && thumbnailSize.height == adjustedBitmap.height) {
                adjustedBitmap
            } else {
                ThumbnailUtils
                    .extractThumbnail(adjustedBitmap, thumbnailSize.width, thumbnailSize.height)
            }
        } catch (ex: OutOfMemoryError) {
            ex.printStackTrace()
            null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    /**
     * create thumbnail for the given [Uri] of video.
     * @param context caller activity/fragment context
     * @param uri saved URI
     * @param thumbnailSize desired thumbnail size
     * @return resized bitmap as desired or null
     * */
    fun getVideoThumbnail(context: Context, uri: Uri?, thumbnailSize: Size): Bitmap? {
        return try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            val frameAtTime = mediaMetadataRetriever.frameAtTime
            if (thumbnailSize.width == frameAtTime?.width && thumbnailSize.height == frameAtTime.height) {
                frameAtTime
            } else {
                ThumbnailUtils
                    .extractThumbnail(frameAtTime, thumbnailSize.width, thumbnailSize.height)
            }
        } catch (ex: OutOfMemoryError) {
            ex.printStackTrace()
            null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    /**
     * create rotated bitmap based on file rotation
     * @param filePath image file Path
     * @return return adjusted bitmap
     * */
    fun getAdjustedBitmap(filePath: String, decodedBitmap: Bitmap): Bitmap? {
        try {
            return when (getOrientation(filePath)) {
                ExifInterface.ORIENTATION_ROTATE_90 ->
                    rotateImage(decodedBitmap, 90f)

                ExifInterface.ORIENTATION_ROTATE_180 ->
                    rotateImage(decodedBitmap, 180f)

                ExifInterface.ORIENTATION_ROTATE_270 ->
                    rotateImage(decodedBitmap, 270f)
                else ->
                    decodedBitmap
            }
        } catch (outOfMemoryError: OutOfMemoryError) {
            outOfMemoryError.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Return file rotation
     * @param filePath image file path
     * */
    private fun getOrientation(filePath: String): Int {
        try {
            val ei = ExifInterface(filePath)
            return ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ExifInterface.ORIENTATION_UNDEFINED
    }

    /**
     * Return rotated bitmap by given angel
     * @param source the original bitmap
     * @param angle the angel to rotate the bitmap
     * */
    private fun rotateImage(source: Bitmap?, angle: Float): Bitmap? {
        if (source == null)
            return null
        return try {
            val matrix = Matrix()
            matrix.postRotate(angle)
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
            null
        }
    }
}