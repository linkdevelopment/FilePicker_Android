package com.linkdev.filepicker_android.pickFilesComponent

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentValues


object FileUtils {
    const val TAG = "FilePickerTag"
    const val CAMERA_IMAGE_TYPE = ".jpg"
    const val CAMERA_VIDEO_TYPE = ".mp4"
    const val IMAG_PREFIX = "IMG_"
    const val VID_PREFIX = "VID_"
    const val MAX_FILE_SIZE_MB = 10
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
        return if (filePath != null) File(filePath) else null
    }

    @Throws(Exception::class)
    private fun copyStream(inputStream: InputStream, outputStream: FileOutputStream) {
        val BUFFER_SIZE = 4096
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
     * Retrieves String file path from a document schemed Uri using fileDescriptor.
     */
    fun getFilePathFromDocument(context: Context, uri: Uri, prefix: String): String? {
        val file: File?
        try {
            file = File.createTempFile(
                prefix + getFileNameFromUri(context, uri),
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

    // reduce file size
    fun writeMedia(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val file = createAppFile(context, fileName)
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
    private fun makeDirectory(context: Context): String {
        val folder = File(
            Environment.getExternalStorageDirectory().absolutePath
                    + "/"
                    + context.getString(com.linkdev.filepicker_android.R.string.app_name)
        )
        if (!folder.exists())
            folder.mkdirs()
        return folder.path
    }

    // get file path in directory
    private fun getFilePath(context: Context, fileName: String?) =
        makeDirectory(context) + "/" + fileName

    // get saved file in created path
    private fun createAppFile(context: Context, fileName: String?): File =
        File(getFilePath(context, fileName))

    fun getUniqueFileName(prefix: String): String =
        prefix + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())

    fun getUniqueFileNameWithExt(prefix: String, extension: String): String =
        getUniqueFileName(prefix) + extension

    fun getPublicStorageDirPath(directory: String): String {
        return Environment.getExternalStoragePublicDirectory(directory)
            .absolutePath
    }

    fun getFileUri(context: Context, fileUrl: String, providerAuth: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, providerAuth, File(fileUrl))
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