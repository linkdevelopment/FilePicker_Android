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

package com.linkdev.filepicker.pickers.video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.utils.file.AndroidQFileUtils
import com.linkdev.filepicker.utils.file.FileUtils
import com.linkdev.filepicker.utils.log.LoggerUtils.logError
import com.linkdev.filepicker.utils.constant.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.mapper.Caller
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.utils.file.FileUtils.VID_PREFIX

/**
 * AndroidQCaptureVideo is a piece of PickFile library to handle open camera action and save recorded video
 * either in the movie folder or given folder in the gallery for android 10
 * @param caller for host fragment/activity
 * @param requestCode to handle [Fragment.onActivityResult]/[Activity.onActivityResult] request code
 * @param folderName the name of directory that captured image will saved into
 * */
internal class CaptureVideoAndroidQ(
    private val caller: Caller,
    private val requestCode: Int,
    private val folderName: String?
) : IPickFilesFactory {
    private var currentCapturedVideoUri: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    //handle action to open camera and get saved URI
    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureVideoIntent.resolveActivity(caller.context.packageManager) != null) {
            currentCapturedVideoUri =
                AndroidQFileUtils.getVideoUri(
                    caller.context, VID_PREFIX, MimeType.MP4, folderName
                )
            currentCapturedVideoUri?.let {
                captureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentCapturedVideoUri)
                try {
                    caller.startActivityForResult(captureVideoIntent, requestCode)
                } catch (ex: SecurityException) {
                    logError(NOT_HANDLED_ERROR_MESSAGE, ex)
                }
            }
        }
    }

    /**
     * used to handle Activity result called on the host view [Fragment.onActivityResult]/[Activity.onActivityResult]
     * @param mRequestCode to identify who this result came from
     * @param resultCode to identify if operation succeeded or canceled
     * @param data return result data to the caller
     * @param callback handle file status
     */
    override fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (mRequestCode == requestCode) {
                if (currentCapturedVideoUri != null) {
                    val fileData = generateFileData(currentCapturedVideoUri!!, data)
                    if (fileData != null)
                        callback.onFilePicked(arrayListOf(fileData))
                    else
                        callback.onPickFileError(
                            ErrorModel(ErrorStatus.URI_ERROR, R.string.file_picker_general_error)
                        )
                } else {
                    callback.onPickFileError(
                        ErrorModel(
                            ErrorStatus.DATA_ERROR, R.string.file_picker_general_error
                        )
                    )
                }
            }
        } else {
            AndroidQFileUtils.deleteUri(caller.context, currentCapturedVideoUri)
        }
    }

    // create File data object
    private fun generateFileData(uri: Uri, data: Intent?): FileData? {
        val filePath = FileUtils.getFilePathFromUri(caller.context, uri)
        val file = FileUtils.getFileFromPath(filePath) // create file
        val fileName = FileUtils.getFullFileNameFromUri(caller.context, uri)
        val mimeType = FileUtils.getFileMimeType(caller.context, uri)
        val fileSize = FileUtils.getFileSize(caller.context, uri)
        return if (filePath.isNullOrBlank() || file == null || mimeType.isNullOrBlank())
            null
        else
            FileData(uri, filePath, file, fileName, mimeType, fileSize, data)
    }
}