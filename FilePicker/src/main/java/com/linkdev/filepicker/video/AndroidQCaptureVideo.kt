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


class AndroidQCaptureVideo(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val folderName: String?
) : IPickFilesFactory {
    private var videoUri: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

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