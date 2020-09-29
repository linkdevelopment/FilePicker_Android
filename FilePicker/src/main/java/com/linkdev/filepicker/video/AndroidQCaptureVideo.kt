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

package com.linkdev.filepicker.video

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
import com.linkdev.filepicker.utils.AndroidQFileUtils
import com.linkdev.filepicker.utils.FileUtils
import com.linkdev.filepicker.utils.LoggerUtils.logError
import com.linkdev.filepicker.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.utils.FileUtils.VID_PREFIX

/**
 * AndroidQCaptureVideo is a piece of PickFile library to handle open camera action and save recorded video
 * either in the movie folder or given folder in the gallery for android 10
 * @param fragment for host fragment
 * @param requestCode to handle [Fragment.onActivityResult] request code
 * @param folderName the name of directory that captured image will saved into
 * */
internal class AndroidQCaptureVideo(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val folderName: String?
) : IPickFilesFactory {
    private var videoUri: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    //handle action to open camera and get saved URI
    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureVideoIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            videoUri =
                AndroidQFileUtils.getVideoUri(
                    fragment.requireContext(), VID_PREFIX, MimeType.MP4, folderName
                )
            videoUri?.let {
                captureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                try {
                    fragment.startActivityForResult(captureVideoIntent, requestCode)
                } catch (ex: SecurityException) {
                    logError(NOT_HANDLED_ERROR_MESSAGE, ex)
                }
            }
        }
    }

    /**
     * used to handle Activity result called on the host view [Fragment.onActivityResult]
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
                if (videoUri != null) {
                    val fileData = generateFileData(videoUri!!, data)
                    if (fileData != null)
                        callback.onFilePicked(arrayListOf(fileData))
                    else
                        callback.onPickFileError(
                            ErrorModel(ErrorStatus.URI_ERROR, R.string.general_error)
                        )
                } else {
                    callback.onPickFileError(
                        ErrorModel(
                            ErrorStatus.DATA_ERROR, R.string.general_error
                        )
                    )
                }
            }
        } else {
            AndroidQFileUtils.deleteUri(fragment.requireContext(), videoUri)
        }
    }

    // create File data object
    private fun generateFileData(uri: Uri, data: Intent?): FileData? {
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