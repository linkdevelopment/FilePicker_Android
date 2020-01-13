package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFileUtils.ErrorStatus.DATA_ERROR
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory


class PickGalleryImage(private val fragment: Fragment) : IPickFilesFactory {

    companion object {
        const val TAG = "FilePickerTag"
        const val PICK_IMAGE_REQUEST_CODE = 1000
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeSet)// get mime type strings
        // make action and set types
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // make chooser and set message
        val chooserIntent = Intent.createChooser(pickIntent, chooserMessage)
        // start activity for result
        fragment.startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == PICK_IMAGE_REQUEST_CODE) {
                val uri = data.data // get uri from data
                if (uri != null) {
                    val filePath =
                        FileUtils.getFilePathFromDocument(
                            fragment.requireContext(), uri
                        ) // get real path of file
                    Log.e(TAG, "file path $filePath")
                    val file = FileUtils.getFileFromPath(filePath) // create file
                    callback.onFilePicked(uri, filePath, file, null)
                } else {
                    callback.onPickFileError(ErrorModel(DATA_ERROR, R.string.pick_file_data_error))
                }
            } else {
                callback.onPickFileError(ErrorModel(DATA_ERROR, R.string.pick_file_data_error))
            }
        } else {
            callback.onPickFileCanceled()
        }
    }
}