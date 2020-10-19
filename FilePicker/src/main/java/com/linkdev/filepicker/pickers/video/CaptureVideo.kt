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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.utils.file.FileUtils
import com.linkdev.filepicker.utils.file.FileUtils.CAMERA_VIDEO_TYPE
import com.linkdev.filepicker.utils.file.FileUtils.VID_PREFIX
import com.linkdev.filepicker.utils.log.LoggerUtils.logError
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.mapper.Caller
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.utils.constant.Constants.ErrorMessages.NOT_HANDLED_CAMERA_ERROR_MESSAGE
import com.linkdev.filepicker.utils.constant.Constants.ErrorMessages.NO_CAMERA_HARDWARE_AVAILABLE_ERROR_MESSAGE
import com.linkdev.filepicker.utils.constant.Constants.ErrorMessages.REQUEST_CODE_ERROR_MESSAGE
import com.linkdev.filepicker.utils.file.AndroidQFileUtils
import com.linkdev.filepicker.utils.file.FileUtilsBelowAndroidQ
import com.linkdev.filepicker.utils.version.Platform
import java.io.File

/**
 * CaptureVideo is a piece of PickFile library to handle open camera action and save recorded video
 * either in the Picture folder or given folder in the gallery
 * @param caller for host fragment/activity
 * @param requestCode to handle [Fragment.onActivityResult]/[Activity.onActivityResult] request code
 * @param allowSyncWithGallery boolean to check if should copy captured video to the gallery
 * @param galleryFolderName the name of directory that captured video will saved into
 * */
internal class CaptureVideo(
    private val caller: Caller,
    private val requestCode: Int,
    private val allowSyncWithGallery: Boolean = false,
    private val galleryFolderName: String? = null
) : IPickFilesFactory {
    private var currentCapturedVideoUri: Uri? = null
    private var currentCapturedVideoPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    //handle action to open camera and saved temporary file and get saved URI
    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureVideoIntent.resolveActivity(caller.context.packageManager) != null) {
            val videoFile = FileUtils.createVideoFile(caller.context)

            currentCapturedVideoPath = videoFile?.path
            currentCapturedVideoUri =
                currentCapturedVideoPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(caller.context, it)
                }

            currentCapturedVideoUri?.let {
                captureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, it)
                if (caller.isCameraPermissionsGranted()) {
                    caller.startActivityForResult(captureVideoIntent, requestCode)
                } else {
                    logError(NOT_HANDLED_CAMERA_ERROR_MESSAGE)
                    throw(SecurityException(NOT_HANDLED_CAMERA_ERROR_MESSAGE))
                }
            }
        } else {
            logError(NO_CAMERA_HARDWARE_AVAILABLE_ERROR_MESSAGE)
            throw(RuntimeException(NO_CAMERA_HARDWARE_AVAILABLE_ERROR_MESSAGE))
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
                if (currentCapturedVideoPath != null && currentCapturedVideoUri != null) {
                    val fileData = generateFileData()
                    if (fileData != null)
                        callback.onFilePicked(arrayListOf(fileData))
                    else
                        callback.onPickFileError(
                            ErrorModel(ErrorStatus.DATA_ERROR, R.string.file_picker_data_error)
                        )
                } else {
                    callback.onPickFileError(
                        ErrorModel(ErrorStatus.FILE_ERROR, R.string.file_picker_file_error)
                    )
                }
            } else {
                logError(REQUEST_CODE_ERROR_MESSAGE, RuntimeException())
            }
        } else {
            FileUtils.deleteUri(caller.context, currentCapturedVideoUri)
            callback.onPickFileCanceled()
        }
    }

    private fun generateFileData(): FileData? {
        val file = File(currentCapturedVideoPath!!)
        val filePath = currentCapturedVideoPath
        val fileName = file.name
        val mimeType = FileUtils.getFileMimeType(caller.context, currentCapturedVideoUri!!)
        val fileSize = FileUtils.getFileSize(caller.context, currentCapturedVideoUri!!)
        return if (filePath.isNullOrBlank() || mimeType.isNullOrBlank())
            null
        else {
            if (allowSyncWithGallery)
                syncWithGallery(file, filePath, fileName)

            FileData(currentCapturedVideoUri!!, filePath, file, fileName, mimeType, fileSize)
        }
    }

    private fun syncWithGallery(file: File, filePath: String, videoName: String) {
        if (Platform.isAndroidQ()) {
            AndroidQFileUtils.saveVideoToGallery(caller.context, file, videoName, galleryFolderName)
        } else {
            FileUtilsBelowAndroidQ.saveVideoToGallery(
                caller.context, currentCapturedVideoUri!!, filePath, galleryFolderName
            )
        }
    }
}