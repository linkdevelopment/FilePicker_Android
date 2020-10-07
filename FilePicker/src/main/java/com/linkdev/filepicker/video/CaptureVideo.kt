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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.utils.FileUtils
import com.linkdev.filepicker.utils.FileUtils.CAMERA_VIDEO_TYPE
import com.linkdev.filepicker.utils.FileUtils.VID_PREFIX
import com.linkdev.filepicker.utils.LoggerUtils.logError
import com.linkdev.filepicker.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.mapper.Caller
import com.linkdev.filepicker.models.ErrorStatus
import java.io.File

/**
 * CaptureVideo is a piece of PickFile library to handle open camera action and save recorded video
 * either in the Picture folder or given folder in the gallery
 * @param caller for host fragment/activity
 * @param requestCode to handle [Fragment.onActivityResult]/[Activity.onActivityResult] request code
 * @param folderName the name of directory that captured image will saved into
 * */
internal class CaptureVideo(
    private val caller: Caller,
    private val requestCode: Int,
    private val folderName: String? = null
) : IPickFilesFactory {
    private var videoUri: Uri? = null
    private var currentCapturedPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    //handle action to open camera and saved temporary file and get saved URI
    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureImageIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureImageIntent.resolveActivity(caller.context.packageManager) != null) {
            val videoFile = FileUtils.createVideoFile(caller.context)
            currentCapturedPath = videoFile?.path
            videoUri =
                currentCapturedPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(caller.context, it)
                }

            videoUri?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, it)
                try {
                    caller.startActivityForResult(captureImageIntent, requestCode)
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
                if (currentCapturedPath != null && videoUri != null) {
                    val fileData = generateFileData(data)
                    if (fileData != null)
                        callback.onFilePicked(arrayListOf(fileData))
                    else
                        callback.onPickFileError(
                            ErrorModel(ErrorStatus.DATA_ERROR, R.string.file_picker_general_error)
                        )
                } else {
                    callback.onPickFileError(
                        ErrorModel(ErrorStatus.URI_ERROR, R.string.file_picker_general_error)
                    )
                }
            } else {
                callback.onPickFileError(
                    ErrorModel(
                        ErrorStatus.DATA_ERROR,
                        R.string.file_picker_general_error
                    )
                )
            }
        } else {
            callback.onPickFileCanceled()
        }
    }

    private fun generateFileData(data: Intent?): FileData? {
        val file = getFile()
        val filePath = file?.path
        val fileName = file?.name
        val mimeType = FileUtils.getFileMimeType(caller.context, videoUri!!)
        val fileSize = FileUtils.getFileSize(caller.context, videoUri!!)
        return if (file == null || filePath.isNullOrBlank() || mimeType.isNullOrBlank())
            null
        else
            FileData(videoUri!!, filePath, file, fileName, mimeType, fileSize, data)
    }

    private fun getFile(): File? {
        val file: File? = if (!folderName.isNullOrBlank()) {
            handleCapturedVideoWithPrivateDir(
                caller.context, videoUri!!, currentCapturedPath!!, folderName
            )

        } else {
            handleCapturedVideoWithPublicDir(caller.context, videoUri!!)
        }

        FileUtils.addMediaToGallery(file, caller.context)
        return file
    }

    private fun handleCapturedVideoWithPublicDir(context: Context, uri: Uri): File? {
        val fileNameWithExt =
            FileUtils.getUniqueFileNameWithExt(VID_PREFIX, CAMERA_VIDEO_TYPE)
        return FileUtils.writePublicFile(context, uri, fileNameWithExt)
    }

    private fun handleCapturedVideoWithPrivateDir(
        context: Context, uri: Uri, currentCapturedPath: String, folderName: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeMedia(context, uri, currentFile.name, folderName)
    }

}