package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory


class CaptureImage(private val fragment: Fragment, private val shouldMakeDir: Boolean = false) :
    IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val DATA_EXTRA = "data"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            if (shouldMakeDir) {

            } else {
                onShouldMakeNoDir(captureImageIntent)
            }
        }
    }

    private fun onShouldMakeDir(intent: Intent) {

    }

    private fun onShouldMakeNoDir(intent: Intent) {
        fragment.startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (shouldMakeDir) {
                    handleCaptureImageWithDir()
                } else {
                    handleCaptureImageWithNoDir(data, callback)
                }
            } else {
                callback.onPickFileError(
                    ErrorModel(
                        PickFileUtils.ErrorStatus.DATA_ERROR, R.string.pick_file_data_error
                    )
                )
            }
        } else {
            callback.onPickFileCanceled()
        }
    }


    private fun handleCaptureImageWithDir() {

    }

    // handle image without save in custom dir/capture from memory
    private fun handleCaptureImageWithNoDir(data: Intent, callback: PickFilesResultCallback) {
        if (data.hasExtra(DATA_EXTRA)) {
            val bitmap = data.extras?.get(DATA_EXTRA) as Bitmap
            val file =
                FileUtils.convertBitmapToFile(fragment.requireContext(), bitmap, 100)
            Log.e(TAG, "file path ${file.absolutePath}")
            callback.onFilePicked(null, file.absolutePath, file, bitmap)
        } else {
            callback.onPickFileError(
                ErrorModel(PickFileUtils.ErrorStatus.DATA_ERROR, R.string.pick_file_data_error)
            )
        }
    }
}