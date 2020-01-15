package com.linkdev.filepicker_android.pickFilesComponent

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.SensorManager.getOrientation
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object FileUtils {
    const val TAG = "FilePickerTag"
    const val CAMERA_IMAGE_TYPE = ".jpg"
    const val IMAG_PREFIX = "IMG_"
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
        return if (filePath != null) File(filePath) else return null
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
            val uniqueFileName = getUniqueFileName()
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
            return File.createTempFile(uniqueFileName, CAMERA_IMAGE_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // create public image file
    fun createPublicImageFile(context: Context): File? {
        try {
            val uniqueFileName = getUniqueFileName()
            val storageDir: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    ?: return null
            return File.createTempFile(uniqueFileName, CAMERA_IMAGE_TYPE, storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // reduce file size
    fun writeImage(context: Context, uri: Uri, fileName: String): File? {
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

    fun writePublicImage(context: Context, uri: Uri, fileName: String): File? {
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

    fun writeBitmapToFile(
        context: Context, bitmap: Bitmap, fileName: String, shouldMakeDir: Boolean
    ): File? {
        return try {
            val createdFile = if (shouldMakeDir) {
                createAppFile(context, fileName)
            } else {
                null
            }
            val fOut = FileOutputStream(createdFile!!)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return createdFile
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

    fun getUniqueFileName(): String =
        IMAG_PREFIX + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())

    fun getUniqueFileNameWithExt(): String =
        getUniqueFileName()  + CAMERA_IMAGE_TYPE

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

    // file operations
    fun getAdjustedBitmap(filePath: String): Bitmap? {
        try {
            val orientation: Int = getOrientation(filePath)
            var adjustedBitmap: Bitmap? = BitmapFactory.decodeFile(filePath)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    adjustedBitmap = rotateImage(adjustedBitmap, 90f)
                    adjustedBitmap = resizeBitmap(adjustedBitmap)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    adjustedBitmap = rotateImage(adjustedBitmap, 180f)
                    adjustedBitmap = resizeBitmap(adjustedBitmap)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    adjustedBitmap = rotateImage(adjustedBitmap, 270f)
                    adjustedBitmap = resizeBitmap(adjustedBitmap)
                }
                ExifInterface.ORIENTATION_NORMAL -> {
                    adjustedBitmap = resizeBitmap(adjustedBitmap)
                }
                else -> {
                    adjustedBitmap = resizeBitmap(adjustedBitmap)
                }
            }
            return adjustedBitmap
        } catch (outOfMemoryError: OutOfMemoryError) {
            outOfMemoryError.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun resizeBitmap(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null)
            return null

        if (bytesToMegaBytes(bitmap.byteCount) < MAX_FILE_SIZE_MB)
            return bitmap

        val width = bitmap.width
        val height = bitmap.height

        var returnedBitmap: Bitmap? = null
        try {
            returnedBitmap = Bitmap.createScaledBitmap(bitmap, width / 2, height / 2, false)
            if (returnedBitmap != null && bytesToMegaBytes(bitmap.byteCount) > MAX_FILE_SIZE_MB) {
                returnedBitmap = resizeBitmap(returnedBitmap)
            }
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
            returnedBitmap = resizeBitmap(returnedBitmap)
        }

        return returnedBitmap
    }

    internal fun getOrientation(filePath: String): Int {
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

    fun rotateImage(filePath: String): Bitmap? {
        val orientation: Int = getOrientation(filePath)
        val bitmap: Bitmap? = getBitmap(filePath)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_NORMAL -> {
                return bitmap
            }
            else -> {
                return bitmap
            }
        }
    }

    private fun getBitmap(filePath: String): Bitmap? {
        try {
            return BitmapFactory.decodeFile(filePath)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return resizeBitmap(BitmapFactory.decodeFile(filePath))
        }
    }

    fun rotateImage(source: Bitmap?, angle: Float): Bitmap? {
        if (source == null)
            return null

        try {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
            )
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
            return null
        }
    }

    fun bytesToMegaBytes(bytes: Int): Double {
        return bytes.toDouble() / 1000000
    }

    // add Image to gallery
    fun addPicToGallery(file: File?, context: Context) {
        file?.let {
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(it.path)
                mediaScanIntent.data = Uri.fromFile(f)
                context.sendBroadcast(mediaScanIntent)
            }
        }

    }
}