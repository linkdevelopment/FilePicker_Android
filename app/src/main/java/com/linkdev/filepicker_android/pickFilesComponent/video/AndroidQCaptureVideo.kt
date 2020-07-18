package com.linkdev.filepicker_android.pickFilesComponent.video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.AndroidQFileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.LoggerUtils.logError
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.ErrorMessages.NOT_HANDLED_ERROR_MESSAGE
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFilesResultCallback


class AndroidQCaptureVideo(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val shouldMakeDir: Boolean
) : IPickFilesFactory {
    private var videoUri: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureVideoIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            videoUri =
                AndroidQFileUtils.getVideoUri(
                    fragment.requireContext(), "VID_", MimeType.MP4, shouldMakeDir
                )
            videoUri?.let {
                //read image from given URI
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
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (mRequestCode == requestCode) {
                if (videoUri != null) {
                    val filePath =
                        FileUtils.getFilePathFromDocument(fragment.requireContext(), videoUri!!)
                    val file = FileUtils.getFileFromPath(filePath)
                    callback.onFilePicked(
                        DocumentFilesType.VIDEO_FILES, videoUri, filePath, file, null
                    )
                } else {
                    callback.onPickFileError(
                        ErrorModel(
                            PickFileConstants.Error.DATA_ERROR, R.string.general_error
                        )
                    )
                }
            }
        }
    }
}