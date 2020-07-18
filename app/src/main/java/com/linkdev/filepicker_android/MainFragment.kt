package com.linkdev.filepicker_android

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FactoryFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.IPickFilesFactory
import com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory.PickFilesFactory
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File


class MainFragment : Fragment() {
    private var pickFilesFactory: IPickFilesFactory? = null

    companion object {
        const val TAG = "FilePickerTag"
        const val CONTENT_PROVIDER_NAME: String = BuildConfig.APPLICATION_ID + ".provider"
        const val PICK_IMAGE_REQUEST_CODE = 1000
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val PICK_VIDEO_REQUEST_CODE = 1002
        const val CAPTURE_VIDEO_REQUEST_CODE = 1003
        const val PICK_TEXT_FILES_REQUEST_CODE = 1004
        const val PICK_AUDIO_REQUEST_CODE = 1005
        const val PICK_ALL_REQUEST_CODE = 1006
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
            pickFilesFactory =
                PickFilesFactory(this, PICK_IMAGE_REQUEST_CODE)
                    .getPickInstance(FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(setOf(MimeType.ALL_IMAGES), "choose Image")
        }
        btnOpenCamera.setOnClickListener {
            pickFilesFactory = PickFilesFactory(
                this, CAPTURE_IMAGE_REQUEST_CODE, true, CONTENT_PROVIDER_NAME
            ).getPickInstance(FactoryFilesType.IMAGE_CAMERA)
            pickFilesFactory?.pickFiles(setOf(MimeType.JPEG), "choose image")
        }
        btnPickVideo.setOnClickListener {
            pickFilesFactory =
                PickFilesFactory(this, PICK_VIDEO_REQUEST_CODE)
                    .getPickInstance(FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(setOf(MimeType.MP4), "choose Image")
        }
        btnCaptureVideo.setOnClickListener {
            pickFilesFactory = PickFilesFactory(
                this, CAPTURE_VIDEO_REQUEST_CODE, true, CONTENT_PROVIDER_NAME
            ).getPickInstance(FactoryFilesType.VIDEO_CAMERA)
            pickFilesFactory?.pickFiles(setOf(MimeType.MP4), "choose Image")
        }
        btnPickFile.setOnClickListener {
            pickFilesFactory = PickFilesFactory(
                this, PICK_TEXT_FILES_REQUEST_CODE, false, CONTENT_PROVIDER_NAME
            ).getPickInstance(FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(setOf(MimeType.PDF), "choose Image")
        }

        btnPickAudio.setOnClickListener {
            pickFilesFactory = PickFilesFactory(
                this, PICK_AUDIO_REQUEST_CODE, false, CONTENT_PROVIDER_NAME
            ).getPickInstance(FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(setOf(MimeType.ALL_AUDIO), "choose Image")
        }

        btnPickAll.setOnClickListener {
            pickFilesFactory = PickFilesFactory(this, PICK_ALL_REQUEST_CODE)
                .getPickInstance(FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(setOf(MimeType.ALL_FILES), "choose Image")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(
            requestCode, resultCode, data, callback = object :
                PickFilesResultCallback {
                override fun onPickFileCanceled() {
                    Log.e(TAG, "onPickFileCanceled")
                }

                override fun onPickFileError(errorModel: ErrorModel) {
                    Log.e(TAG, "onPickFileError")
                }

                override fun onFilePicked(
                    fileType: DocumentFilesType,
                    uri: Uri?,
                    filePath: String?,
                    file: File?,
                    bitmap: Bitmap?
                ) {
                    Log.e(TAG, "onFilePicked")
                    Log.e(TAG, "file path from view $filePath Uri is $uri")
                    when (fileType) {
                        DocumentFilesType.IMAGE_FILES ->
                            Glide.with(requireContext())
                                .load(file)
                                .into(imgTest)
                        DocumentFilesType.VIDEO_FILES -> {
                            vidTest.setVideoURI(uri)
                            vidTest.start()
                        }
                        DocumentFilesType.AUDIO_FILE -> {
                            val mp = MediaPlayer()
                            try {
                                mp.setDataSource(filePath)
                                mp.prepare()
                                mp.start()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                        else -> {
                            Log.e(TAG, fileType.toString())
                        }
                    }
                }

            })
    }
}