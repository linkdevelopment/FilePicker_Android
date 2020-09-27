package com.linkdev.filepicker_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.factory.PickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.layout_capture.*
import kotlinx.android.synthetic.main.layout_pick.*


class MainFragment : Fragment() {
    private var pickFilesFactory: IPickFilesFactory? = null
    private lateinit var attachedFilesAdapter: AttachedFilesAdapter
    private lateinit var mimeTypesAdapter: MimeTypesAdapter

    companion object {
        const val TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val CAPTURE_VIDEO_REQUEST_CODE = 1003
        const val PICK_ALL_REQUEST_CODE = 1004
        const val IMAGES_FOLDER_NAME = "File Picker_Images"
        const val VIDEOS_FOLDER_NAME = "File Picker_Videos"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        setListeners()
    }

    private fun initViews() {
        initAttachFilesRecyclerView()
        initMimeTypesRecyclerView()
        collapseExpandSection(captureFlow, imgCaptureArrow)
    }

    private fun initAttachFilesRecyclerView() {
        attachedFilesAdapter = AttachedFilesAdapter(requireContext())
        rvFiles.layoutManager = LinearLayoutManager(requireContext())
        rvFiles.adapter = attachedFilesAdapter
    }

    private fun initMimeTypesRecyclerView() {
        mimeTypesAdapter = MimeTypesAdapter(requireContext())
        rvMimeTypes.layoutManager = GridLayoutManager(requireContext(), 2)
        rvMimeTypes.adapter = mimeTypesAdapter
    }

    private fun setListeners() {
        btnCapturePhoto.setOnClickListener {
            onCapturePhotoClicked()
        }

        btnRecordVideo.setOnClickListener {
            onRecordVideoClicked()
        }

        btnPickFiles.setOnClickListener {
            onPickFilesClicked()
        }

        layoutCapture.setOnClickListener {
            collapseExpandSection(captureFlow, imgCaptureArrow)
        }

        layoutPick.setOnClickListener {
            collapseExpandSection(pickGroup, imgPickArrow)
        }

        layoutSelectionType.setOnClickListener {
            collapseExpandSection(rgSelectionTypes, imgSelectionArrow)
        }

        layoutMimeTypes.setOnClickListener {
            collapseExpandSection(rvMimeTypes, imgMimeTypesArrow)
        }
    }

    // check permission and open camera to capture photo
    private fun onCapturePhotoClicked() {
        if (arePermissionsGranted(getCameraPermissionsList())) {
            pickFilesFactory = PickFilesFactory(
                this, CAPTURE_IMAGE_REQUEST_CODE, IMAGES_FOLDER_NAME
            ).getPickInstance(FactoryFilesType.IMAGE_CAMERA)
            pickFilesFactory?.pickFiles()
        } else {
            requestPermissionsCompat(getCameraPermissionsList(), CAPTURE_IMAGE_REQUEST_CODE)
        }
    }

    // check permission and open camera to record video
    private fun onRecordVideoClicked() {
        if (arePermissionsGranted(getCameraPermissionsList())) {
            pickFilesFactory = PickFilesFactory(
                this, CAPTURE_VIDEO_REQUEST_CODE, VIDEOS_FOLDER_NAME
            ).getPickInstance(FactoryFilesType.VIDEO_CAMERA)
            pickFilesFactory?.pickFiles()
        } else {
            requestPermissionsCompat(getCameraPermissionsList(), CAPTURE_VIDEO_REQUEST_CODE)
        }
    }

    /**
     * send list of mime types [com.linkdev.filepicker.models.MimeType]
     * or will allow all files mime types [com.linkdev.filepicker.models.MimeType.ALL_FILES]
     * check storage permission and open documents
     * */
    private fun onPickFilesClicked() {
        if (arePermissionsGranted(getStoragePermissionList())) {
            pickFilesFactory = PickFilesFactory(
                fragment = this,
                requestCode = PICK_ALL_REQUEST_CODE,
                selectionType = getSelectionType()
            ).getPickInstance(FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(arrayListOf(MimeType.ALL_FILES))
        } else {
            requestPermissionsCompat(getStoragePermissionList(), PICK_ALL_REQUEST_CODE)
        }
    }

    private fun collapseExpandSection(sectionView: View, arrowImage: ImageView) {
        if (sectionView.visibility == VISIBLE) {
            sectionView.visibility = GONE
            arrowImage.setImageResource(R.drawable.ic_arrow_right)
        } else {
            sectionView.visibility = VISIBLE
            arrowImage.setImageResource(R.drawable.ic_arrow_drop_down)
        }
    }

    /**
     * selection type [com.linkdev.filepicker.models.SelectionTypes] by default it is[com.linkdev.filepicker.models.SelectionTypes.SINGLE]
     * */
    private fun getSelectionType(): SelectionTypes {
        return if (rbMultipleSelection.isChecked)
            SelectionTypes.MULTIPLE
        else
            SelectionTypes.SINGLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(requestCode, resultCode, data, pickFilesCallback)
    }

    private val pickFilesCallback = object :
        PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            Log.e(TAG, "onPickFileCanceled")
        }

        override fun onPickFileError(errorModel: ErrorModel) {
            Log.e(TAG, "onPickFileError")
        }

        override fun onFilePicked(fileData: ArrayList<FileData>) {
            Log.e(TAG, "onFilePicked")
            attachedFilesAdapter.replaceFiles(fileData)
        }
    }

    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    private fun requestPermissionsCompat(
        permissions: Array<String>,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
    }

    private fun getCameraPermissionsList(): Array<String> {
        return arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    private fun getStoragePermissionList(): Array<String> {
        return arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}