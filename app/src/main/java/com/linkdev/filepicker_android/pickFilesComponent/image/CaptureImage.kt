package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.R
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.FileUtils.IMAG_PREFIX
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.Error.DATA_ERROR
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFileConstants.RequestCodes.CAPTURE_IMAGE_REQUEST_CODE
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.utils.AndroidQFileUtils
import com.linkdev.filepicker_android.pickFilesComponent.utils.Platform
import java.io.File


class CaptureImage(
    private val fragment: Fragment,
    private val shouldMakeDir: Boolean,
    private val contentProviderName: String?
) : IPickFilesFactory {
    private var currentCapturedPath: String? = null
    private var photoURI: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            photoURI =
                AndroidQFileUtils.getPhotoUri(fragment.requireContext(), "IMG_", MimeType.JPEG)
            photoURI?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                fragment.startActivityForResult(captureImageIntent, CAPTURE_IMAGE_REQUEST_CODE)
            }
        }
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (photoURI != null) {
                    val file =
                        handleCapturedImageWithPublicDir(fragment.requireContext(), photoURI!!)
                    callback.onFilePicked(
                        DocumentFilesType.IMAGE_FILES, photoURI, file?.path, file, null
                    )
                }
            }
        }
    }

    private fun handleCapturedImageWithPublicDir(context: Context, uri: Uri): File? {
        val fileNameWithExt =
            FileUtils.getUniqueFileNameWithExt(IMAG_PREFIX, FileUtils.CAMERA_IMAGE_TYPE)
        return AndroidQFileUtils.writePublicFile(context, uri, fileNameWithExt)
    }

    private fun handleCapturedImageWithPrivateDir(
        context: Context, uri: Uri, currentCapturedPath: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeMedia(context, uri, currentFile.name)
    }
}