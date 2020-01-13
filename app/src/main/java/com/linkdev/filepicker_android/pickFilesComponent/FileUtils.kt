package com.linkdev.filepicker_android.pickFilesComponent

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.image.CaptureImage
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object FileUtils {
    const val TAG = "FilePickerTag"
    const val CAMERA_IMAGE_TYPE = ".jpg"
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
                context.getString(R.string.app_name) + getFileNameFromUri(
                    context,
                    uri
                ),
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

    fun getRealPathFromUri(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            return if (cursor != null) {
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(column_index)
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${context.getString(R.string.app_name)}_$timeStamp", /* prefix */
            CAMERA_IMAGE_TYPE, /* suffix */
            storageDir /* directory */
        )
    }

    fun getFileURI(context: Context, providerAuth: String, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context, context.packageName + providerAuth, file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    fun convertBitmapToFile(context: Context, bitmap: Bitmap, quality: Int): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val filesDir = context.getFilesDir()
        val imageFile =
            File(filesDir, "${context.getString(R.string.app_name)}_$timeStamp$CAMERA_IMAGE_TYPE")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error writing bitmap", e)
        }

        return imageFile
    }
}