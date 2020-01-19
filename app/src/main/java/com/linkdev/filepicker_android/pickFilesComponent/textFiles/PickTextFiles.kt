package com.linkdev.filepicker_android.pickFilesComponent.textFiles

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFileConstants.Error.DATA_ERROR
import com.linkdev.filepicker_android.pickFilesComponent.PickFileConstants.RequestCodes.PICK_TEXT_FILES_REQUEST_CODE
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory


class PickTextFiles(private val fragment: Fragment) : IPickFilesFactory {

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        if (mimeTypeSet.isEmpty()) throw Exception("File Picker Error, MIME type cannot be empty")
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeSet)// get mime type strings
        // make action and set types
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL_FILES.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // make chooser and set message
        val chooserIntent = Intent.createChooser(pickIntent, chooserMessage)
        // start activity for result
        fragment.startActivityForResult(chooserIntent, PICK_TEXT_FILES_REQUEST_CODE)
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == PICK_TEXT_FILES_REQUEST_CODE) {
                val uri = data.data // get uri from data
                if (uri != null) {
                    val filePath =
                        FileUtils.getFilePathFromDocument(
                            fragment.requireContext(), uri, "FILE_"
                        ) // get real path of file
                    Log.e(TAG, "file path $filePath")
                    val file = FileUtils.getFileFromPath(filePath) // create file
                    callback.onFilePicked(FilesType.TEXT_FILE, uri, filePath, file, null)
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