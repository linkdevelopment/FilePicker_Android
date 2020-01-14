package com.linkdev.filepicker_android.pickFilesComponent

import android.content.ContentResolver
import android.content.Context
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
import com.linkdev.filepicker_android.BuildConfig
import com.linkdev.filepicker_android.R
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

    // convert bitmap to file
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

    fun getAppFolder(): String {
        val folder = File(
            Environment.getExternalStorageDirectory().absolutePath
                    + "/"
                    + "FilePicker"
        )
        folder.mkdirs()
        return folder.path
    }

    fun getAppFilePath(fileName: String?) =
        getAppFolder() + "/" + fileName

    fun createAppFile(fileName: String?): File {
        return File(getAppFilePath(fileName))
    }

    fun createImageFile(context: Context, fileName: String): File? {
        try {
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
            return File(storageDir.path, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun writeBitmapToFile(bitmap: Bitmap, fileName: String): File? {
        try {
            val file = createAppFile(fileName)
            val fOut = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getFileName(filePath: String): String = File(filePath).name

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

        if (bytesToMegaBytes(bitmap.byteCount) < 10)
            return bitmap

        val width = bitmap.width
        val height = bitmap.height

        var returnedBitmap: Bitmap? = null
        try {
            returnedBitmap = Bitmap.createScaledBitmap(bitmap, width / 2, height / 2, false)
            if (returnedBitmap != null && bytesToMegaBytes(bitmap.byteCount) > 10) {
                returnedBitmap = resizeBitmap(returnedBitmap)
            }
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
            returnedBitmap = resizeBitmap(returnedBitmap)
        }

        return returnedBitmap
    }

    fun bytesToMegaBytes(bytes: Int): Double {
        return bytes.toDouble() / 1000000
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

    fun isImageMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image")
    }

    fun geImageName(extension: String = CAMERA_IMAGE_TYPE): String =
        "IMG_" + SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.ENGLISH
        ).format(Date()) + extension

    fun getFileUri(context: Context, fileUrl: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, context.packageName + ".provider", File(fileUrl))
        } else {
            Uri.fromFile(File(fileUrl))
        }
    }
}