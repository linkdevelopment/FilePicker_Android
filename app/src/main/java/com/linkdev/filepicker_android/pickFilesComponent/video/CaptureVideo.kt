package com.linkdev.filepicker_android.pickFilesComponent.video

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils.CAMERA_VIDEO_TYPE
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils.VID_PREFIX
import com.linkdev.filepicker_android.pickFilesComponent.PickFileConstants.RequestCodes.CAPTURE_VIDEO_REQUEST_CODE
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FactoryFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import java.io.File

class CaptureVideo(
    private val fragment: Fragment,
    private val shouldMakeDir: Boolean,
    private val contentProviderName: String?
) : IPickFilesFactory {
    private var videoUri: Uri? = null
    private var currentCapturedPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            val videoFile = FileUtils.createVideoFile(fragment.requireContext())
            currentCapturedPath = videoFile?.path
            if (contentProviderName.isNullOrBlank())
                throw Exception("File Picker Error, Please add FileProvider authorities")

            videoUri =
                currentCapturedPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(
                        fragment.requireContext(), it, contentProviderName
                    )
                }

            videoUri?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, it)
                fragment.startActivityForResult(captureImageIntent, CAPTURE_VIDEO_REQUEST_CODE)
            }
        }

    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_VIDEO_REQUEST_CODE) {
                if (currentCapturedPath != null && videoUri != null) {

                    val file: File? = if (shouldMakeDir) {
                        handleCapturedVideoWithPrivateDir(
                            fragment.requireContext(), videoUri!!, currentCapturedPath!!
                        )

                    } else {
                        handleCapturedVideoWithPublicDir(fragment.requireContext(), videoUri!!)
                    }

                    FileUtils.addMediaToGallery(file, fragment.requireContext())

                    callback.onFilePicked(
                        DocumentFilesType.VIDEO_FILES, videoUri, file?.path, file, null
                    )
                } else {
                    callback.onPickFileError(ErrorModel())
                }
            } else {
                callback.onPickFileError(ErrorModel())
            }
        } else {
            callback.onPickFileCanceled()
        }
    }

    private fun handleCapturedVideoWithPublicDir(context: Context, uri: Uri): File? {
        val fileNameWithExt =
            FileUtils.getUniqueFileNameWithExt(VID_PREFIX, CAMERA_VIDEO_TYPE)
        return FileUtils.writePublicFile(context, uri, fileNameWithExt)
    }

    private fun handleCapturedVideoWithPrivateDir(
        context: Context, uri: Uri, currentCapturedPath: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeMedia(context, uri, currentFile.name)
    }

}