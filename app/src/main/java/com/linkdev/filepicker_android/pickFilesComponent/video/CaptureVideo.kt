package com.linkdev.filepicker_android.pickFilesComponent.video

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import java.io.File

class CaptureVideo(private val fragment: Fragment, private val shouldMakeDir: Boolean) :
    IPickFilesFactory {
    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_VIDEO_REQUEST_CODE = 1003
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivityForResult(captureImageIntent, CAPTURE_VIDEO_REQUEST_CODE)
        }

    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_VIDEO_REQUEST_CODE && data != null && data.data != null) {
                val videoUri = data.data!!

                val realPathFromUri =
                    FileUtils.getRealPathFromUri(fragment.requireContext(), videoUri)
                val file = realPathFromUri?.let { File(realPathFromUri) }

                callback.onFilePicked(FilesType.VIDEO_CAMERA, videoUri, realPathFromUri, file, null)
            } else {
                callback.onPickFileError(ErrorModel())
            }
        } else {
            callback.onPickFileCanceled()
        }
    }
}