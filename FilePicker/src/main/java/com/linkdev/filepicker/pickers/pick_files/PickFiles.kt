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

package com.linkdev.filepicker.pickers.pick_files

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.mapper.Caller
import com.linkdev.filepicker.models.*
import com.linkdev.filepicker.utils.constant.Constants.ErrorMessages.NOT_HANDLED_DOCUMENT_ERROR_MESSAGE
import com.linkdev.filepicker.utils.constant.Constants.ErrorMessages.REQUEST_CODE_ERROR_MESSAGE
import com.linkdev.filepicker.utils.file.FileUtils
import com.linkdev.filepicker.utils.log.LoggerUtils.logError

/**
 * Class used to open document, select files and handle selected file status
 * @param caller host view fragment/Activity
 * @param requestCode to handle [Fragment.onActivityResult]/[Activity.onActivityResult] request code
 * @param selectionMode refers to [SelectionMode] including two types [SelectionMode.SINGLE] and [SelectionMode.MULTIPLE]
 *used to indicate that an [Intent.ACTION_OPEN_DOCUMENT] can allow the user to select and return multiple items
 * @param thumbnailSize refers to [Size] class for thumbnail custom size
 */
internal class PickFiles(
    private val caller: Caller,
    private val requestCode: Int,
    private val selectionMode: SelectionMode,
    private val thumbnailSize: Size
) : IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
    }

    /** open document library with acceptable MIME types and handle the option of multiple and
     *  single selection and throw exception if required runtime permission not handled.
     *
     * @param mimeTypeList used to communicate a set of acceptable MIME types
     * @throws SecurityException Throw exception if [Manifest.permission.READ_EXTERNAL_STORAGE] not handled in runtime or not allowed.
     */
    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val mimeType = MimeType.getArrayOfMimeType(mimeTypeList)// get mime type strings
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = MimeType.ALL_FILES.mimeTypeName
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
        if (selectionMode == SelectionMode.MULTIPLE)
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        if (caller.isDocumentPermissionsGranted()) {
            caller.startActivityForResult(pickIntent, requestCode)
        } else {
            logError(NOT_HANDLED_DOCUMENT_ERROR_MESSAGE)
            throw (Throwable(SecurityException(NOT_HANDLED_DOCUMENT_ERROR_MESSAGE)))
        }
    }

    /**
     * used to handle Activity result called on the caller [Fragment.onActivityResult]/[Activity.onActivityResult]
     * @param mRequestCode to identify who this result came from
     * @param resultCode to identify if operation succeeded or canceled
     * @param data return result data to the caller
     * @param callback refers to [PickFilesStatusCallback] used to get the status of the Action canceled by the user ,
     * error occurred while picking the data or done successfully and data retrieved.
     */
    override fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && mRequestCode == requestCode) {
                if (selectionMode == SelectionMode.MULTIPLE && data.clipData != null) {
                    onMultipleSelection(data, callback)
                } else {
                    onSingleSelection(data, callback)
                }
            } else {
                logError(REQUEST_CODE_ERROR_MESSAGE, RuntimeException())
            }
        } else {
            callback.onPickFileCanceled()
        }
    }

    /** used when allow the user to select and return multiple items.
     *
     * @param data return result data to the caller. use [Intent.getClipData] to return array of URIs
     * @param callback refers to [PickFilesStatusCallback] used to get the status of the Action canceled by the user ,
     * error occurred while retrieving the data or done successfully and data retrieved.
     */
    private fun onMultipleSelection(data: Intent, callback: PickFilesStatusCallback) {
        val clipData = data.clipData!!
        if (clipData.itemCount > 0) {
            val pickedFilesList = ArrayList<FileData>()
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                val fileData = generateFileData(uri)
                if (fileData == null) {
                    callback.onPickFileError(
                        ErrorModel(ErrorStatus.DATA_ERROR, R.string.file_picker_data_error)
                    )
                } else {
                    pickedFilesList.add(fileData)
                }
            }
            callback.onFilePicked(pickedFilesList)
        } else {
            callback.onPickFileError(
                ErrorModel(ErrorStatus.PICK_ERROR, R.string.file_picker_general_error)
            )
        }
    }

    /** used when allow the user to select and return only one item.
     *
     * @param data return result data to the caller. use [Intent.getClipData] to return array of URIs
     * @param callback refers to [PickFilesStatusCallback] used to get the status of the Action canceled by the user ,
     * error occurred while retrieving the data or done successfully and data retrieved.
     */
    private fun onSingleSelection(data: Intent, callback: PickFilesStatusCallback) {
        val uri = data.data
        if (uri != null) {
            val fileData = generateFileData(uri)
            if (fileData == null) {
                callback.onPickFileError(
                    ErrorModel(ErrorStatus.DATA_ERROR, R.string.file_picker_data_error)
                )
            } else {
                callback.onFilePicked(arrayListOf(fileData))
            }
        } else {
            callback.onPickFileError(
                ErrorModel(ErrorStatus.PICK_ERROR, R.string.file_picker_pick_error)
            )
        }
    }

    /**Return [FileData] that contains selected file-specific information. If there is error occurred
     * while retrieving file data returns null
     */
    private fun generateFileData(uri: Uri): FileData? {
        val filePath = FileUtils.getFilePathFromUri(caller.context, uri)
        val file = FileUtils.getFileFromPath(filePath) // create file
        val fileName = FileUtils.getFullFileNameFromUri(caller.context, uri)
        val mimeType = FileUtils.getFileMimeType(caller.context, uri)
        val fileSize = FileUtils.getFileSize(caller.context, uri)
        return if (filePath.isNullOrBlank() || file == null || mimeType.isNullOrBlank())
            null
        else
            FileData(uri, filePath, file, fileName, mimeType, fileSize, getThumbnail(mimeType, uri))
    }

    /** Returns [Bitmap] thumbnail if selected file is Image or Video and return null if not.*/
    private fun getThumbnail(mimeType: String, uri: Uri): Bitmap? {
        return when {
            MimeType.isImage(mimeType) ->
                FileUtils.getImageThumbnail(caller.context, uri, thumbnailSize)
            MimeType.isVideo(mimeType) ->
                FileUtils.getVideoThumbnail(caller.context, uri, thumbnailSize)
            else -> null
        }
    }
}