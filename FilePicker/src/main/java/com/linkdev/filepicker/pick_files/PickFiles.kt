/*
 * Copyright (C) 2020 Link Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkdev.filepicker.pick_files

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.*
import com.linkdev.filepicker.utils.FileUtils
import com.linkdev.filepicker.utils.LoggerUtils.logError
import com.linkdev.filepicker.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE

/**
 * Class used to open document, select files and handle selected file status
 * @param fragment host view
 * @param requestCode to handle [Fragment.onActivityResult] request code
 * @param selectionType refers to [SelectionTypes] for [Intent.ACTION_OPEN_DOCUMENT] selection type and
 *                   by default is single selection*/
internal class PickFiles(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val selectionType: SelectionTypes
) : IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeList)// get mime type strings
        // make action and set types
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL_FILES.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        if (selectionType == SelectionTypes.MULTIPLE)
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // start activity for result
        try {
            fragment.startActivityForResult(pickIntent, requestCode)
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
            FileData(uri, filePath, file, fileName, mimeType, data)
    }
}