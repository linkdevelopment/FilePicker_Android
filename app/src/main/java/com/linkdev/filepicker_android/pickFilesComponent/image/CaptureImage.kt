package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import java.io.File


class CaptureImage(private val fragment: Fragment, private val shouldMakeDir: Boolean) :
    IPickFilesFactory {
    private var currentCapturedPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val DATA_EXTRA = "data"
        const val PROVIDER_AUTH = ".provider"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile =
            FileUtils.createImageFile(fragment.requireContext(), FileUtils.geImageName())
        currentCapturedPath = imageFile?.path

        val photoURI =
            currentCapturedPath?.let { FileUtils.getFileUri(fragment.requireContext(), it) }

        photoURI?.let {
            captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            fragment.startActivityForResult(captureImageIntent, CAPTURE_IMAGE_REQUEST_CODE)
        }
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (currentCapturedPath != null) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                    val uri =
                        FileUtils.getFileUri(fragment.requireContext(), currentCapturedPath!!)
                    val adjustedBitmap = FileUtils.getAdjustedBitmap(currentCapturedPath!!)
                    val file = adjustedBitmap?.let {
                        FileUtils.writeBitmapToFile(adjustedBitmap, FileUtils.geImageName())
                    }
                    callback.onFilePicked(uri, file?.path, file, adjustedBitmap)
                } else {
                    callback.onPickFileCanceled()
                }
            }
        } else {
            callback.onPickFileError(ErrorModel("error current pic is null"))
        }
    }
}