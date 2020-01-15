package com.linkdev.filepicker_android.pickFilesComponent.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.FileUtils
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import java.io.File


class CaptureImage(private val fragment: Fragment, private val shouldMakeDir: Boolean) :
    IPickFilesFactory {
    private var currentCapturedPath: String? = null

    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val PROVIDER_AUTH = ".provider"
    }

    override fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String) {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(fragment.requireContext().packageManager) != null) {
            val imageFile = createImageFile()

            currentCapturedPath = imageFile?.path

            val photoURI =
                currentCapturedPath?.let {
                    // get photo uri form content provider
                    FileUtils.getFileUri(
                        fragment.requireContext(), it, PROVIDER_AUTH
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
            FileUtils.createPublicImageFile(fragment.requireContext())
        }
    }

    override fun handleActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                if (currentCapturedPath != null) {

                    val uri =
                        FileUtils.getFileUri(
                            fragment.requireContext(), currentCapturedPath!!, PROVIDER_AUTH
                        )

                    val file: File? = if (shouldMakeDir) {
                        handleCapturedImageWithPrivateDir(
                            fragment.requireContext(), uri, currentCapturedPath!!
                        )

                    } else {
                        handleCapturedImageWithPublicDir(fragment.requireContext(), uri)
                    }

                    FileUtils.addPicToGallery(file, fragment.requireContext())

                    callback.onFilePicked(uri, file?.path, file, null)
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
        val fileNameWithExt = FileUtils.getUniqueFileNameWithExt()
        return FileUtils.writePublicImage(context, uri, fileNameWithExt)
    }

    private fun handleCapturedImageWithPrivateDir(
        context: Context, uri: Uri, currentCapturedPath: String
    ): File? {
        val currentFile = File(currentCapturedPath)
        return FileUtils.writeImage(context, uri, currentFile.name)
    }
}