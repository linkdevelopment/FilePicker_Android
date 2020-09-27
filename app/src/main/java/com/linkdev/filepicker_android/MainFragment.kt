package com.linkdev.filepicker_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.factory.PickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.*
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment() {
    private var pickFilesFactory: IPickFilesFactory? = null

    companion object {
        const val TAG = "FilePickerTag"
        const val PICK_IMAGE_REQUEST_CODE = 1000
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val PICK_VIDEO_REQUEST_CODE = 1002
        const val CAPTURE_VIDEO_REQUEST_CODE = 1003
        const val PICK_TEXT_FILES_REQUEST_CODE = 1004
        const val PICK_AUDIO_REQUEST_CODE = 1005
        const val PICK_ALL_REQUEST_CODE = 1006
        const val IMAGES_FOLDER_NAME = "File Picker Images"
        const val VIDEOS_FOLDER_NAME = "File Picker Videos"
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
        btnCapturePhoto.setOnClickListener {
            pickFilesFactory = PickFilesFactory(
                this, CAPTURE_IMAGE_REQUEST_CODE, IMAGES_FOLDER_NAME
            ).getPickInstance(FactoryFilesType.IMAGE_CAMERA)
            pickFilesFactory?.pickFiles(arrayListOf(MimeType.JPEG), "choose image")
        }

        btnRecordVideo.setOnClickListener {
            pickFilesFactory = PickFilesFactory(
                this, CAPTURE_VIDEO_REQUEST_CODE, VIDEOS_FOLDER_NAME
            ).getPickInstance(FactoryFilesType.VIDEO_CAMERA)
            pickFilesFactory?.pickFiles(arrayListOf(MimeType.MP4), "choose Image")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(
            requestCode, resultCode, data, callback = object :
                PickFilesStatusCallback {
                override fun onPickFileCanceled() {
                    Log.e(TAG, "onPickFileCanceled")
                }

                override fun onPickFileError(errorModel: ErrorModel) {
                    Log.e(TAG, "onPickFileError")
                }

                override fun onFilePicked(fileData: ArrayList<FileData>) {
                    Log.e(TAG, "onFilePicked")
                    Log.e(TAG, "file data size $fileData")
                }
            })
    }
}