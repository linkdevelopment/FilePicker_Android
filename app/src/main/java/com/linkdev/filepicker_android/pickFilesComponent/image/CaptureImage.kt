package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils.CAMERA_IMAGE_TYPE
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils.IMAG_PREFIX
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import java.io.File


class CaptureImage(
    private val fragment: Fragment,
    private val shouldMakeDir: Boolean,
    private val contentProviderName: String
) : IPickFilesFactory {
    private var currentCapturedPath: String? = null
    private var photoURI: Uri? = null

    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            val imageFile = createImageFile()

            currentCapturedPath = imageFile?.path

            photoURI =
                currentCapturedPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(
                        fragment.requireContext(), it, contentProviderName
                    )
                }

            photoURI?.let {
                //read image from given URI
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                fragment.startActivityForResult(captureImageIntent, CAPTURE_IMAGE_REQUEST_CODE)
            }
        }
    }

    // create image file in private dir or public pictures dir
    private fun createImageFile(): File? {
        return if (shouldMakeDir) {
            FileUtils.createImageFile(fragment.requireContext())
        } else {
            FileUtils.createPublicFile(fragment.requireContext(), IMAG_PREFIX, CAMERA_IMAGE_TYPE)
        }
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (currentCapturedPath != null && photoURI != null) {

                    val file: File? = if (shouldMakeDir) {
                        handleCapturedImageWithPrivateDir(
                            fragment.requireContext(), photoURI!!, currentCapturedPath!!
                        )

                    } else {
                        handleCapturedImageWithPublicDir(fragment.requireContext(), photoURI!!)
                    }

                    FileUtils.addMediaToGallery(file, fragment.requireContext())

                    callback.onFilePicked(FilesType.IMAGE_CAMERA, photoURI, file?.path, file, null)
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

    private fun handleCapturedImageWithPublicDir(context: Context, uri: Uri): File? {
        val fileNameWithExt =
            FileUtils.getUniqueFileNameWithExt(IMAG_PREFIX, FileUtils.CAMERA_IMAGE_TYPE)
        return FileUtils.writePublicFile(context, uri, fileNameWithExt)
    }

    private fun handleCapturedImageWithPrivateDir(
        context: Context, uri: Uri, currentCapturedPath: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeMedia(context, uri, currentFile.name)
    }
}