/*
 * Copyright (C) 2020 Link Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkdev.filepicker_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.factory.PickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.*
import com.linkdev.filepicker_android.adapters.AttachedFilesAdapter
import com.linkdev.filepicker_android.adapters.MimeTypesAdapter
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.layout_capture.*
import kotlinx.android.synthetic.main.layout_pick.*
import kotlinx.android.synthetic.main.layout_picked_files.*


class MainFragment : Fragment() {
    private var pickFilesFactory: IPickFilesFactory? = null
    private lateinit var attachedFilesAdapter: AttachedFilesAdapter
    private lateinit var mimeTypesAdapter: MimeTypesAdapter

    companion object {
        const val TAG = "MainFragment"
        const val LOG_TAG = "FilePickerTag"
        const val CAPTURE_IMAGE_REQUEST_CODE = 1001
        const val CAPTURE_VIDEO_REQUEST_CODE = 1003
        const val PICK_ALL_REQUEST_CODE = 1004
        const val IMAGES_FOLDER_NAME = "File Picker_Images"
        const val VIDEOS_FOLDER_NAME = "File Picker_Videos"
        fun newInstance(): MainFragment = MainFragment()
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
        collapseExpandSection(captureSection, tvCapture)
    }

    private fun initAttachFilesRecyclerView() {
        attachedFilesAdapter =
            AttachedFilesAdapter(
                requireContext()
            )
        rvFiles.layoutManager = LinearLayoutManager(requireContext())
        rvFiles.adapter = attachedFilesAdapter
    }

    private fun initMimeTypesRecyclerView() {
        mimeTypesAdapter =
            MimeTypesAdapter(
                requireContext()
            )
        mimeTypesSection.layoutManager = GridLayoutManager(requireContext(), 2)
        mimeTypesSection.adapter = mimeTypesAdapter
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

        crdLayoutCapture.setOnClickListener {
            collapseExpandSection(captureSection, tvCapture)
            collapseExpandSection(pickSection, tvPick, true)
        }

        crdLayoutPick.setOnClickListener {
            collapseExpandSection(pickSection, tvPick)
            collapseExpandSection(captureSection, tvCapture, true)
        }

        tvSelectionMode.setOnClickListener {
            collapseExpandSection(selectionModeSection, tvSelectionMode)
        }

        tvMimeTypes.setOnClickListener {
            collapseExpandSection(mimeTypesSection, tvMimeTypes)
        }
    }

    /**
     * check permission and open camera to capture photo
     * init [pickFilesFactory] by [PickFilesFactory.getInstance]
     * */
    // TODO handle WRITE_EXTERNAL_STORAGE and CAMERA runtime permission
    private fun onCapturePhotoClicked() {
        if (checkPermissions(getCameraPermissionsList())) {
            pickFilesFactory = PickFilesFactory(
                caller = this,
                requestCode = CAPTURE_IMAGE_REQUEST_CODE,
                galleryFolderName = IMAGES_FOLDER_NAME,
                allowSyncWithGallery = chbAllowSyncWithGallery.isChecked,
                thumbnailSize = Size(500, 500)
            ).getInstance(FileTypes.CAPTURE_IMAGE)
            pickFilesFactory?.pickFiles()
        } else {
            requestPermission(getCameraPermissionsList(), CAPTURE_IMAGE_REQUEST_CODE)
        }
    }

    /**
     * check permission and open camera to record video
     * init [pickFilesFactory] by [PickFilesFactory.getInstance]
     * */
    // TODO handle WRITE_EXTERNAL_STORAGE and CAMERA runtime permission
    private fun onRecordVideoClicked() {
        if (checkPermissions(getCameraPermissionsList())) {
            pickFilesFactory = PickFilesFactory(
                caller = this,
                requestCode = CAPTURE_VIDEO_REQUEST_CODE,
                galleryFolderName = VIDEOS_FOLDER_NAME,
                allowSyncWithGallery = chbAllowSyncWithGallery.isChecked,
                thumbnailSize = Size(500, 500)
            ).getInstance(FileTypes.CAPTURE_VIDEO)
            pickFilesFactory?.pickFiles()
        } else {
            requestPermission(getCameraPermissionsList(), CAPTURE_VIDEO_REQUEST_CODE)
        }
    }

    /**
     * check storage permission and open documents
     * init [pickFilesFactory] by  [PickFilesFactory.getInstance]
     * */
    // TODO handle READ_EXTERNAL_STORAGE runtime permission
    private fun onPickFilesClicked() {
        if (checkPermissions(getStoragePermissionList())) {
            pickFilesFactory = PickFilesFactory(
                caller = this,
                requestCode = PICK_ALL_REQUEST_CODE,
                selectionMode = getSelectionMode(),
                thumbnailSize = Size(500, 500)
            ).getInstance(fileTypes = FileTypes.PICK_FILES)
            pickFilesFactory?.pickFiles(mimeTypeList = getMimeTypesList())
        } else {
            requestPermission(getStoragePermissionList(), PICK_ALL_REQUEST_CODE)
        }
    }

    /**
     * selection type [com.linkdev.filepicker.models.SelectionMode]
     * */
    private fun getSelectionMode(): SelectionMode {
        return if (rbMultipleSelection.isChecked)
            SelectionMode.MULTIPLE
        else
            SelectionMode.SINGLE
    }

    /**
     * return supported list of mime types [com.linkdev.filepicker.models.MimeType]
     * or by default will allow all files mime types [com.linkdev.filepicker.models.MimeType.ALL_FILES]
     * */
    private fun getMimeTypesList(): ArrayList<MimeType> {
        return mimeTypesAdapter.getCheckedMimeTypeList()
    }

    /**
     * call [IPickFilesFactory.handleActivityResult] and handle status using [PickFilesStatusCallback]
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(requestCode, resultCode, data, pickFilesCallback)
        showHideSections(requestCode)
    }

    private val pickFilesCallback = object :
        PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            Toast.makeText(requireContext(), "Action canceled", Toast.LENGTH_SHORT).show()
        }

        override fun onPickFileError(errorModel: ErrorModel) {
            when (errorModel.errorStatus) {
                ErrorStatus.DATA_ERROR -> showToastMessage(errorModel.defaultErrorMessage)
                ErrorStatus.FILE_ERROR -> showToastMessage(errorModel.defaultErrorMessage)
                ErrorStatus.PICK_ERROR -> showToastMessage(errorModel.defaultErrorMessage)
            }
        }

        override fun onFilePicked(fileData: ArrayList<FileData>) {
            attachedFilesAdapter.replaceFiles(fileData)
            layoutPickedFiles.visibility = VISIBLE
        }
    }

    private fun showHideSections(requestCode: Int) {
        collapseExpandSection(mimeTypesSection, tvMimeTypes, true)
        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE || requestCode == CAPTURE_VIDEO_REQUEST_CODE) {
            collapseExpandSection(pickSection, tvPick, true)
        } else {
            collapseExpandSection(captureSection, tvCapture, true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var allPermissionGranted = true
        for (permission: Int in grantResults)
            if (permission == PackageManager.PERMISSION_DENIED) {
                allPermissionGranted = false
            }

        if (allPermissionGranted) {
            when (requestCode) {
                CAPTURE_IMAGE_REQUEST_CODE -> onCapturePhotoClicked()
                CAPTURE_VIDEO_REQUEST_CODE -> onRecordVideoClicked()
                PICK_ALL_REQUEST_CODE -> onPickFilesClicked()
            }
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    private fun requestPermission(
        permissions: Array<String>,
        requestCode: Int
    ) {
        requestPermissions(permissions, requestCode)
    }

    private fun getCameraPermissionsList(): Array<String> {
        return arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    private fun getStoragePermissionList(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun collapseExpandSection(
        sectionView: View,
        headerSection: TextView,
        forceCollapse: Boolean = false
    ) {
        if (sectionView.visibility == VISIBLE || forceCollapse) {
            sectionView.visibility = GONE
            headerSection
                .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        } else {
            sectionView.visibility = VISIBLE
            headerSection
                .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0)
        }
    }

    private fun showToastMessage(@StringRes message: Int) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}