package com.linkdev.filepicker_android.pickFilesComponent.video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.AndroidQFileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.RequestCodes.CAPTURE_VIDEO_REQUEST_CODE
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFilesResultCallback


class AndroidQCaptureVideo(
    private val fragment: Fragment, private val shouldMakeDir: Boolean
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
                fragment.startActivityForResult(captureVideoIntent, CAPTURE_VIDEO_REQUEST_CODE)
            }
        }
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_VIDEO_REQUEST_CODE) {
                if (videoUri != null) {
                    val filePath =
                        FileUtils.getFilePathFromDocument(fragment.requireContext(), videoUri!!)
                    val file = FileUtils.getFileFromPath(filePath)
                    callback.onFilePicked(
                        DocumentFilesType.VIDEO_FILES, videoUri, filePath, file, null
                    )
                }
            }
        }
    }
}