package com.linkdev.filepicker_android.pickFilesComponent.allFiles

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFileConstants
import com.linkdev.filepicker_android.pickFilesComponent.PickFileConstants.RequestCodes.PICK_ALL_REQUEST_CODE
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory

class AllFiles(private val fragment: Fragment) : IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeSet)// get mime type strings
        // make action and set types
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL_FILES.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // make chooser and set message
        val chooserIntent = Intent.createChooser(pickIntent, chooserMessage)
        // start activity for result
        fragment.startActivityForResult(
            chooserIntent, PICK_ALL_REQUEST_CODE
        )
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && requestCode == PICK_ALL_REQUEST_CODE) {
                val uri = data.data // get uri from data
                if (uri != null) {
                    val filePath =
                        FileUtils.getFilePathFromDocument(
                            fragment.requireContext(), uri
                        ) // get real path of file
                    val fileType =
                        FileUtils.getFileTypeFromUri(fragment.requireContext(), uri)
                    val file = FileUtils.getFileFromPath(filePath) // create file
                    callback.onFilePicked(fileType, uri, filePath, file, null)
                } else {
                    callback.onPickFileError(
                        ErrorModel(
                            PickFileConstants.Error.URI_ERROR, R.string.general_error
                        )
                    )
                }
            } else {
                callback.onPickFileError(
                    ErrorModel(
                        PickFileConstants.Error.DATA_ERROR, R.string.general_error
                    )
                )
            }
        } else {
            callback.onPickFileCanceled()
        }
    }
}