package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.RequestCodes.CAPTURE_IMAGE_REQUEST_CODE
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.*
import com.linkdev.filepicker_android.pickFilesComponent.utils.LoggerUtils.logError
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE


class AndroidQCaptureImage(
    private val fragment: Fragment, private val shouldMakeDir: Boolean
) : IPickFilesFactory {
    private var photoURI: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            photoURI =
                AndroidQFileUtils.getPhotoUri(
                    fragment.requireContext(), "IMG_", MimeType.JPEG, shouldMakeDir
                )
            photoURI?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                try {
                    fragment.startActivityForResult(captureImageIntent, CAPTURE_IMAGE_REQUEST_CODE)
                } catch (ex: SecurityException) {
                    logError(NOT_HANDLED_ERROR_MESSAGE, ex)

                }
            }
        }
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (photoURI != null) {
                    val filePath =
                        FileUtils.getFilePathFromDocument(fragment.requireContext(), photoURI!!)
                    val file = FileUtils.getFileFromPath(filePath)
                    callback.onFilePicked(
                        DocumentFilesType.IMAGE_FILES, photoURI, filePath, file, null
                    )
                }
            }
        }
    }
}