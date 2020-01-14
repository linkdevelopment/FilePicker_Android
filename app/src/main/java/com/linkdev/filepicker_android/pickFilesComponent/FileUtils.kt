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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


object FileUtils {
    const val TAG = "FilePickerTag"
    const val CAMERA_IMAGE_TYPE = ".jpg"
    const val IMAG_PREFIX = "IMG_"
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
                context.getString(com.linkdev.filepicker_android.R.string.app_name) + getFileNameFromUri(
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

    // convert bitmap to file
    fun convertBitmapToFile(context: Context, bitmap: Bitmap, quality: Int): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val filesDir = context.getFilesDir()
        val imageFile =
            File(
                filesDir,
                "${context.getString(com.linkdev.filepicker_android.R.string.app_name)}_$timeStamp$CAMERA_IMAGE_TYPE"
            )
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

    // create image file
    fun createImageFile(context: Context): File? {
        try {
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
            val uniqueFileName = getUniqueFileName()
            return File.createTempFile(uniqueFileName, CAMERA_IMAGE_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // reduce file size
    fun compressImage(context: Context, uri: Uri, fileName: String): File? {
        val file = createAppFile(context, fileName)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val outputStream = FileOutputStream(file)
            copyStream(inputStream, outputStream)
            return file
        }
        return null
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

    fun getUniqueFileName(): String =
        IMAG_PREFIX + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())


    fun getFileUri(context: Context, fileUrl: String, providerAuth: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, context.packageName + providerAuth, File(fileUrl))
        } else {
            Uri.fromFile(File(fileUrl))
        }
    }
}