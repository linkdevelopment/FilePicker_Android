package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.interactions.PickFilesStatusCallback
import com.linkdev.filepicker_android.pickFilesComponent.models.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.models.FileData
import com.linkdev.filepicker_android.pickFilesComponent.models.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.*
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils.IMAG_PREFIX
import com.linkdev.filepicker_android.pickFilesComponent.utils.LoggerUtils.logError
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE


class AndroidQCaptureImage(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val shouldMakeDir: Boolean,
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
                    fragment.requireContext(), IMAG_PREFIX, MimeType.JPEG, shouldMakeDir
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
                    callback.onFilePicked(fileData)
                } else {
                    callback.onPickFileError(
                        ErrorModel(
                            PickFileConstants.Error.DATA_ERROR, R.string.general_error
                        )
                    )
                }
            }
        }
    }
}