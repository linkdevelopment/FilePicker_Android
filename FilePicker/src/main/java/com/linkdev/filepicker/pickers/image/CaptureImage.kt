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

package com.linkdev.filepicker.pickers.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.R
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.mapper.Caller
import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.ErrorStatus
import com.linkdev.filepicker.models.FileData
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.utils.constant.PickFileConstants
import com.linkdev.filepicker.utils.constant.PickFileConstants.ErrorMessages.NOT_HANDLED_CAMERA_ERROR_MESSAGE
import com.linkdev.filepicker.utils.constant.PickFileConstants.ErrorMessages.NO_CAMERA_HARDWARE_AVAILABLE_ERROR_MESSAGE
import com.linkdev.filepicker.utils.constant.PickFileConstants.ErrorMessages.REQUEST_CODE_ERROR_MESSAGE
import com.linkdev.filepicker.utils.file.FileUtils
import com.linkdev.filepicker.utils.file.FileUtils.IMAG_PREFIX
import com.linkdev.filepicker.utils.log.LoggerUtils.logError
import java.io.File

/**
 * CaptureImage is a piece of PickFile library to handle open camera action and save captured imaged
 * either in the Picture folder or given folder in the gallery
 * @param caller for host fragment/activity
 * @param requestCode to handle [Fragment.onActivityResult]/[Activity.onActivityResult] request code
 * @param galleryFolderName the name of directory that captured image will saved into
 * */
internal class CaptureImage(
    private val caller: Caller,
    private var requestCode: Int,
    private val allowSyncWithGallery: Boolean = false,
    private val galleryFolderName: String? = null,
    private val thumbnailSize: Size
) : IPickFilesFactory {
    private var currentCapturedImagePath: String? = null
    private var currentCapturedImageURI: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    //handle action to open camera and saved temporary file and get saved URI
    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (captureImageIntent.resolveActivity(caller.context.packageManager) != null) {
            // Create the temp File where the photo should saved
            val imageFile = FileUtils.createImageFile(caller.context)

            currentCapturedImagePath = imageFile?.path
            currentCapturedImageURI =
                currentCapturedImagePath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(caller.context, it)
                }

            currentCapturedImageURI?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentCapturedImageURI)
                if (caller.isCameraPermissionsGranted()) {
                    caller.startActivityForResult(captureImageIntent, requestCode)
                } else {
                    logError(NOT_HANDLED_CAMERA_ERROR_MESSAGE, SecurityException())
                }
            }
        } else {
            logError(NO_CAMERA_HARDWARE_AVAILABLE_ERROR_MESSAGE, RuntimeException())
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
                if (currentCapturedImagePath != null && currentCapturedImageURI != null) {
                    val fileData = generateFileData(data)
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
            FileUtils.deleteUri(caller.context, currentCapturedImageURI)
            callback.onPickFileCanceled()
        }
    }

    /**
     * generate file data object
     * @return [FileData]
     * */
    private fun generateFileData(data: Intent?): FileData? {
        val file = File(currentCapturedImagePath!!)
        val filePath = currentCapturedImagePath
        val fileName = file.name
        val mimeType = FileUtils.getFileMimeType(caller.context, currentCapturedImageURI!!)
        val fileSize = FileUtils.getFileSize(caller.context, currentCapturedImageURI!!)
        return if (filePath.isNullOrBlank() || mimeType.isNullOrBlank())
            null
        else {
            if (allowSyncWithGallery)
                syncWithGallery()
            val thumbnail = FileUtils
                .getImageThumbnail(caller.context, currentCapturedImageURI!!, thumbnailSize)
            FileData(
                currentCapturedImageURI!!, filePath, file, fileName, mimeType, fileSize, thumbnail
            )
        }
    }

    /**
     * @return [File] saved file using [copyCapturedPhotoToPictureFolder] or [copyCapturedPhotoToGalleryFolder]
     * */
    private fun syncWithGallery() {
        val file: File? = if (!galleryFolderName.isNullOrBlank()) {
            copyCapturedPhotoToGalleryFolder(
                caller.context,
                currentCapturedImageURI!!,
                currentCapturedImagePath!!,
                galleryFolderName
            )

        } else {
            copyCapturedPhotoToPictureFolder(caller.context, currentCapturedImageURI!!)
        }

        FileUtils.addMediaToGallery(file, caller.context)
    }

    /**
     * save captured image in default folder
     * @return [File]
     * */
    private fun copyCapturedPhotoToPictureFolder(context: Context, uri: Uri): File? {
        val fileNameWithExt =
            FileUtils.getUniqueFileNameWithExt(IMAG_PREFIX, FileUtils.CAMERA_IMAGE_TYPE)
        return FileUtils.writePublicFile(context, uri, fileNameWithExt)
    }

    /**
     * save captured image in given folder name
     * @return [File]
     * */
    private fun copyCapturedPhotoToGalleryFolder(
        context: Context, uri: Uri, currentCapturedPath: String, folderName: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeMedia(context, uri, currentFile.name, folderName)
    }
}