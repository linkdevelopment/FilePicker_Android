package com.linkdev.filepicker_android

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.image.CaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.PickFilesFactory
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File

class MainFragment : Fragment() {
    private var pickFilesFactory: IPickFilesFactory? = null

    companion object {
        const val TAG = "FilePickerTag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        btnPickImage.setOnClickListener {
            pickFilesFactory = PickFilesFactory(this).getPickInstance(FilesType.IMAGE_GALLERY)
            pickFilesFactory?.pickFiles(setOf(MimeType.PNG), "choose Image")
        }
        btnOpenCamera.setOnClickListener {
            pickFilesFactory = PickFilesFactory(this, true).getPickInstance(FilesType.IMAGE_CAMERA)
            pickFilesFactory?.pickFiles(setOf(MimeType.ALL), "choose image")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(
            requestCode, resultCode, data, callback = object : PickFilesResultCallback {
                override fun onPickFileCanceled() {
                    Log.e(TAG, "onPickFileCanceled")
                }

                override fun onPickFileError(errorModel: ErrorModel) {
                    Log.e(TAG, "onPickFileError")
                }

                override fun onFilePicked(
                    uri: Uri?, filePath: String?, file: File?, bitmap: Bitmap?
                ) {
                    Log.e(TAG, "onFilePicked")
                    Log.e(TAG, "file path from view $filePath Uri is $uri")

                    uri?.let { imgTest.setImageURI(it) }
//                    bitmap?.let { imgTest.setImageBitmap(it) }
                }

            })
    }
}