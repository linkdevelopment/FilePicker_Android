package com.linkdev.filepicker_android.pickFilesComponent.allFiles

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants
import com.linkdev.filepicker_android.pickFilesComponent.interactions.PickFilesStatusCallback
import com.linkdev.filepicker_android.pickFilesComponent.models.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.models.FileData
import com.linkdev.filepicker_android.pickFilesComponent.models.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.LoggerUtils.logError
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE

class AllFiles(private val fragment: Fragment, private val requestCode: Int) : IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeList: ArrayList<MimeType>, chooserMessage: String) {
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeList)// get mime type strings
        // make action and set types
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL_FILES.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // make chooser and set message
        val chooserIntent = Intent.createChooser(pickIntent, chooserMessage)
        // start activity for result
        try {
            fragment.startActivityForResult(chooserIntent, requestCode)
        } catch (ex: SecurityException) {
            logError(NOT_HANDLED_ERROR_MESSAGE, ex)
        }
    }

    override fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && mRequestCode == requestCode) {
                val uri = data.data // get uri from data
                if (uri != null) {
                    val filePath =
                        FileUtils.getFilePathFromUri(
                            fragment.requireContext(), uri
                        ) // get real path of file
                    val file = FileUtils.getFileFromPath(filePath) // create file
                    val fileData = FileData(uri, filePath, file, null)
                    callback.onFilePicked(fileData)
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