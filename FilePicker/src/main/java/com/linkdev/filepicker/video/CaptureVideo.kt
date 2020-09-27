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
import com.linkdev.filepicker.models.ErrorStatus
import java.io.File

class CaptureVideo(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val folderName: String? = null
) : IPickFilesFactory {
    private var videoUri: Uri? = null
    private var currentCapturedPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeList: ArrayList<MimeType>) {
        val captureImageIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            val videoFile = FileUtils.createVideoFile(fragment.requireContext())
            currentCapturedPath = videoFile?.path
            videoUri =
                currentCapturedPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(fragment.requireContext(), it)
                }

            videoUri?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, it)
                try {
                    fragment.startActivityForResult(captureImageIntent, requestCode)
                } catch (ex: SecurityException) {
                    logError(NOT_HANDLED_ERROR_MESSAGE, ex)
                }
            }
        }

    }

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
                            ErrorModel(ErrorStatus.DATA_ERROR, R.string.general_error)
                        )
                } else {
                    callback.onPickFileError(
                        ErrorModel(ErrorStatus.URI_ERROR, R.string.general_error)
                    )
                }
            } else {
                callback.onPickFileError(ErrorModel(ErrorStatus.DATA_ERROR, R.string.general_error))
            }
        } else {
            callback.onPickFileCanceled()
        }
    }

    private fun generateFileData(data: Intent?): FileData? {
        val file = getFile()
        val filePath = file?.path
        val fileName = FileUtils.getFullFileNameFromUri(fragment.requireContext(), videoUri!!)
        val mimeType = FileUtils.getFileMimeType(fragment.requireContext(), videoUri!!)
        return if (file == null || filePath.isNullOrBlank() || mimeType.isNullOrBlank())
            null
        else
            FileData(videoUri!!, filePath, file, fileName, mimeType, data)
    }

    private fun getFile(): File? {
        val file: File? = if (!folderName.isNullOrBlank()) {
            handleCapturedVideoWithPrivateDir(
                fragment.requireContext(), videoUri!!, currentCapturedPath!!, folderName
            )

        } else {
            handleCapturedVideoWithPublicDir(fragment.requireContext(), videoUri!!)
        }

        FileUtils.addMediaToGallery(file, fragment.requireContext())
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