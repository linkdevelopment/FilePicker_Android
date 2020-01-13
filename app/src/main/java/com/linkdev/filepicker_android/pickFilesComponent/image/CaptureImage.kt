package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFileUtils.ErrorStatus.DATA_ERROR
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import java.io.File
import java.io.IOException


class CaptureImage(private val fragment: Fragment, private val shouldMakeDir: Boolean) :
    IPickFilesFactory {
    private var currentCapturedPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val DATA_EXTRA = "data"
        const val PROVIDER_AUTH = ".fileProvider"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            if (shouldMakeDir) {
                onShouldMakeDir(captureImageIntent)
            } else {
                onShouldMakeNoDir(captureImageIntent)
            }
        }
    }

    // start activity with make dir
    private fun onShouldMakeDir(intent: Intent) {
        val imageFile: File? = try {
            FileUtils.createImageFile(fragment.requireContext())
        } catch (ex: IOException) {
            null
        }
        imageFile?.let {
            val photoURI = FileUtils.getFileURI(fragment.requireContext(), PROVIDER_AUTH, it)
            Log.e(TAG, "photo URI $photoURI")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            fragment.startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
        }
        currentCapturedPath = imageFile?.path

    }

    // start activity without make dir
    private fun onShouldMakeNoDir(intent: Intent) {
        fragment.startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (shouldMakeDir) {
                    handleCaptureImageWithDir(callback)
                } else {
                    handleCaptureImageWithNoDir(data, callback)
                }
            } else {
                callback.onPickFileError(
                    ErrorModel(DATA_ERROR, R.string.pick_file_data_error)
                )
            }
        } else {
            callback.onPickFileCanceled()
        }
    }


    private fun handleCaptureImageWithDir(callback: PickFilesResultCallback) {
        if (currentCapturedPath == null) {
            callback.onPickFileError(ErrorModel(DATA_ERROR, R.string.pick_file_data_error))
        } else {
            val file = FileUtils.getFileFromPath(currentCapturedPath)
            val uri =
                file?.let { FileUtils.getFileURI(fragment.requireContext(), PROVIDER_AUTH, it) }
            callback.onFilePicked(uri, currentCapturedPath, file, null)
        }
    }

    // handle image without save in custom dir/capture from memory
    private fun handleCaptureImageWithNoDir(data: Intent?, callback: PickFilesResultCallback) {
        if (data != null) {
            if (data.hasExtra(DATA_EXTRA)) {
                val bitmap = data.extras?.get(DATA_EXTRA) as Bitmap
                val file =
                    FileUtils.convertBitmapToFile(fragment.requireContext(), bitmap, 100)
                callback.onFilePicked(null, file.absolutePath, file, bitmap)
            } else {
                callback.onPickFileError(
                    ErrorModel(DATA_ERROR, R.string.pick_file_data_error)
                )
            }
        } else {
            callback.onPickFileError(ErrorModel())
        }
    }
}