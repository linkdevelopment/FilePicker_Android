package com.linkdev.filepicker_android.pickFilesComponent.allFiles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants
import com.linkdev.filepicker_android.pickFilesComponent.interactions.PickFilesStatusCallback
import com.linkdev.filepicker_android.pickFilesComponent.models.*
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.LoggerUtils.logError
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE

class AllFiles(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val selectionType: SelectionTypes
) : IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeList: ArrayList<MimeType>, chooserMessage: String) {
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeList)// get mime type strings
        // make action and set types
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL_FILES.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        if (selectionType == SelectionTypes.MULTIPLE)
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
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
                if (selectionType == SelectionTypes.MULTIPLE && data.clipData != null) {
                    onMultipleSelection(data, callback)
                } else {
                    onSingleSelection(data, callback)
                }
            } else {
                callback.onPickFileError(
                    ErrorModel(ErrorStatus.DATA_ERROR, R.string.general_error)
                )
            }
        } else {
            callback.onPickFileCanceled()
        }
    }

    private fun onMultipleSelection(data: Intent, callback: PickFilesStatusCallback) {
        val clipData = data.clipData!!
        if (clipData.itemCount > 0) {
            val pickedFilesList = ArrayList<FileData>()
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                val fileData = generateFileData(uri, data)
                if (fileData == null) {
                    callback.onPickFileError(
                        ErrorModel(ErrorStatus.ATTACH_ERROR, R.string.general_error)
                    )
                } else {
                    pickedFilesList.add(fileData)
                }
            }
            callback.onFilePicked(pickedFilesList)
        } else {
            callback.onPickFileError(
                ErrorModel(ErrorStatus.DATA_ERROR, R.string.general_error)
            )
        }
    }

    // handle single Selection logic
    private fun onSingleSelection(data: Intent, callback: PickFilesStatusCallback) {
        val uri = data.data
        if (uri != null) {
            val fileData = generateFileData(uri, data)
            if (fileData == null) {
                callback.onPickFileError(
                    ErrorModel(ErrorStatus.ATTACH_ERROR, R.string.general_error)
                )
            } else {
                val pickedFilesList = ArrayList<FileData>()
                pickedFilesList.add(fileData)
                callback.onFilePicked(pickedFilesList)
            }
        } else {
            callback.onPickFileError(
                ErrorModel(ErrorStatus.URI_ERROR, R.string.general_error)
            )
        }
    }

    // create File data object
    private fun generateFileData(uri: Uri, data: Intent): FileData? {
        val filePath = FileUtils.getFilePathFromUri(fragment.requireContext(), uri)
        val file = FileUtils.getFileFromPath(filePath) // create file
        val fileName = FileUtils.getFullFileNameFromUri(fragment.requireContext(), uri)
        val mimeType = FileUtils.getFileMimeType(fragment.requireContext(), uri)
        return if (filePath.isNullOrBlank() || file == null || mimeType.isNullOrBlank())
            null
        else
            FileData(uri = uri, filePath = filePath, file = file, intent = data)
    }
}