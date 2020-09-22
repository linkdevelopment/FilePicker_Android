package com.linkdev.filepicker.image

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.R
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.utils.*
import com.linkdev.filepicker.utils.FileUtils.IMAG_PREFIX
import com.linkdev.filepicker.utils.LoggerUtils.logError
import com.linkdev.filepicker.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE


class AndroidQCaptureImage(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val folderName: String?
) : IPickFilesFactory {
    private var photoURI: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeList: ArrayList<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            photoURI =
                AndroidQFileUtils.getPhotoUri(
                    fragment.requireContext(), IMAG_PREFIX, MimeType.JPEG, folderName
                )
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
                if (photoURI != null) {
                    val filePath =
                        FileUtils.getFilePathFromUri(fragment.requireContext(), photoURI!!)
                    val file = FileUtils.getFileFromPath(filePath)
                    val fileData =
                        FileData(photoURI, filePath, file, null)
                    callback.onFilePicked(arrayListOf(fileData))
                } else {
                    callback.onPickFileError(
                        ErrorModel(
                            ErrorStatus.DATA_ERROR, R.string.general_error
                        )
                    )
                }
            }
        } else {
            AndroidQFileUtils.deleteUri(fragment.requireContext(), photoURI)
        }
    }
}