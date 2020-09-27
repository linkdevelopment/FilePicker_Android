package com.linkdev.filepicker.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.utils.FileUtils
import com.linkdev.filepicker.utils.FileUtils.IMAG_PREFIX
import com.linkdev.filepicker.utils.LoggerUtils.logError
import com.linkdev.filepicker.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE
import java.io.File


class CaptureImage(
    private val fragment: Fragment,
    private var requestCode: Int,
    private val folderName: String? = null
) : IPickFilesFactory {
    private var currentCapturedPath: String? = null
    private var photoURI: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            // Create the File where the photo should go
            val imageFile = FileUtils.createImageFile(fragment.requireContext())

            currentCapturedPath = imageFile?.path
            photoURI =
                currentCapturedPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(fragment.requireContext(), it)
                }

            photoURI?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                try {
                    fragment.startActivityForResult(captureImageIntent, requestCode)
                } catch (ex: SecurityException) {
                    logError(NOT_HANDLED_ERROR_MESSAGE, ex)
                }
            }
        }
    }

    override fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (mRequestCode == requestCode) {
                if (currentCapturedPath != null && photoURI != null) {
                    val fileData = generateFileData(data)
                    if (fileData != null)
                        callback.onFilePicked(arrayListOf(fileData))
                    else
                        callback.onPickFileError(
                            ErrorModel(ErrorStatus.DATA_ERROR, R.string.general_error)
                        )
                } else {
                    callback.onPickFileError(
                        ErrorModel(ErrorStatus.URI_ERROR, R.string.general_error)
                    )
                }
            } else {
                callback.onPickFileError(ErrorModel(ErrorStatus.DATA_ERROR, R.string.general_error))
            }
        } else {
            callback.onPickFileCanceled()
        }
    }

    private fun generateFileData(data: Intent?): FileData? {
        val file = getFile()
        val filePath = file?.path
        val fileName = file?.name
        val mimeType = FileUtils.getFileMimeType(fragment.requireContext(), photoURI!!)
        return if (file == null || filePath.isNullOrBlank() || mimeType.isNullOrBlank())
            null
        else
            FileData(photoURI!!, filePath, file, fileName, mimeType, data)
    }

    private fun getFile(): File? {
        val file: File? = if (!folderName.isNullOrBlank()) {
            handleCapturedImageWithPrivateDir(
                fragment.requireContext(), photoURI!!, currentCapturedPath!!, folderName
            )

        } else {
            handleCapturedImageWithPublicDir(fragment.requireContext(), photoURI!!)
        }

        FileUtils.addMediaToGallery(file, fragment.requireContext())
        return file
    }

    private fun handleCapturedImageWithPublicDir(context: Context, uri: Uri): File? {
        val fileNameWithExt =
            FileUtils.getUniqueFileNameWithExt(IMAG_PREFIX, FileUtils.CAMERA_IMAGE_TYPE)
        return FileUtils.writePublicFile(context, uri, fileNameWithExt)
    }

    private fun handleCapturedImageWithPrivateDir(
        context: Context, uri: Uri, currentCapturedPath: String, folderName: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeMedia(context, uri, currentFile.name, folderName)
    }
}