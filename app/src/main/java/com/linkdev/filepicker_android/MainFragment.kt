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
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkdev.filepicker.factory.IPickFilesFactory
import com.linkdev.filepicker.factory.PickFilesFactory
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.*
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
        collapseExpandSection(captureFlow, tvCapture)
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

        crdLayoutCapture.setOnClickListener {
            collapseExpandSection(captureFlow, tvCapture)
            collapseExpandSection(pickGroup, tvPick, true)
        }

        crdLayoutPick.setOnClickListener {
            collapseExpandSection(pickGroup, tvPick)
            collapseExpandSection(captureFlow, tvCapture, true)
        }

        tvSelectionType.setOnClickListener {
            collapseExpandSection(rgSelectionTypes, tvSelectionType)
        }

        tvMimeTypes.setOnClickListener {
            collapseExpandSection(rvMimeTypes, tvMimeTypes)
        }
    }

    /**
     * check permission and open camera to capture photo
     * init [pickFilesFactory] by [PickFilesFactory.getPickInstance]
     * */
    private fun onCapturePhotoClicked() {
        if (arePermissionsGranted(getCameraPermissionsList())) {
            pickFilesFactory = PickFilesFactory(
                fragment = this,
                requestCode = CAPTURE_IMAGE_REQUEST_CODE,
                folderName = IMAGES_FOLDER_NAME
            ).getPickInstance(FactoryFilesType.IMAGE_CAMERA)
            pickFilesFactory?.pickFiles()
        } else {
            requestPermission(getCameraPermissionsList(), CAPTURE_IMAGE_REQUEST_CODE)
        }
    }

    /**
     * check permission and open camera to record video
     * init [pickFilesFactory] by [PickFilesFactory.getPickInstance]
     * */
    private fun onRecordVideoClicked() {
        if (arePermissionsGranted(getCameraPermissionsList())) {
            pickFilesFactory = PickFilesFactory(
                fragment = this,
                requestCode = CAPTURE_VIDEO_REQUEST_CODE,
                folderName = VIDEOS_FOLDER_NAME
            ).getPickInstance(FactoryFilesType.VIDEO_CAMERA)
            pickFilesFactory?.pickFiles()
        } else {
            requestPermission(getCameraPermissionsList(), CAPTURE_VIDEO_REQUEST_CODE)
        }
    }

    /**
     * check storage permission and open documents
     * init [pickFilesFactory] by  [PickFilesFactory.getPickInstance]
     * */
    private fun onPickFilesClicked() {
        if (arePermissionsGranted(getStoragePermissionList())) {
            pickFilesFactory = PickFilesFactory(
                fragment = this,
                requestCode = PICK_ALL_REQUEST_CODE,
                selectionType = getSelectionType()
            ).getPickInstance(fileType = FactoryFilesType.PICK_FILES)
            pickFilesFactory?.pickFiles(mimeTypeList = getMimeTypesList())
        } else {
            requestPermission(getStoragePermissionList(), PICK_ALL_REQUEST_CODE)
        }
    }

    /**
     * selection type [com.linkdev.filepicker.models.SelectionTypes]
     * */
    private fun getSelectionType(): SelectionTypes {
        return if (rbMultipleSelection.isChecked)
            SelectionTypes.MULTIPLE
        else
            SelectionTypes.SINGLE
    }

    /**
     * return support list of mime types [com.linkdev.filepicker.models.MimeType]
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
        collapseExpandSection(rvMimeTypes, tvMimeTypes, true)
        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE || requestCode == CAPTURE_VIDEO_REQUEST_CODE) {
            collapseExpandSection(pickGroup, tvPick, true)
        } else {
            collapseExpandSection(captureFlow, tvCapture, true)
        }
    }

    private val pickFilesCallback = object :
        PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            Toast.makeText(requireContext(), "user cancel pick files", Toast.LENGTH_SHORT).show()
        }

        override fun onPickFileError(errorModel: ErrorModel) {
            Log.e(LOG_TAG, "onPickFileError")
            Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT).show()
        }

        override fun onFilePicked(fileData: ArrayList<FileData>) {
            Log.e(LOG_TAG, "onFilePicked: $fileData")
            attachedFilesAdapter.replaceFiles(fileData)
            layoutPickedFiles.visibility = VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                CAPTURE_IMAGE_REQUEST_CODE -> onCapturePhotoClicked()
                CAPTURE_VIDEO_REQUEST_CODE -> onRecordVideoClicked()
                PICK_ALL_REQUEST_CODE -> onPickFilesClicked()
            }
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun collapseExpandSection(
        sectionView: View,
        headerTextView: TextView,
        forceCollapse: Boolean = false
    ) {
        if (sectionView.visibility == VISIBLE || forceCollapse) {
            sectionView.visibility = GONE
            headerTextView
                .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        } else {
            sectionView.visibility = VISIBLE
            headerTextView
                .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0)
        }
    }
}