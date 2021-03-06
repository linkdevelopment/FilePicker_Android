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

package com.linkdev.filepicker.factory

import android.content.Intent
import android.util.Size
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.pickers.image.CaptureImage
import com.linkdev.filepicker.mapper.Caller
import com.linkdev.filepicker.models.FileTypes
import com.linkdev.filepicker.models.SelectionMode
import com.linkdev.filepicker.pickers.pick_files.PickFiles
import com.linkdev.filepicker.pickers.video.CaptureVideo

/**
 * Class used to provide instance of each pick file type by [getInstance] and check [FileTypes]
 * @param caller host Fragment/Activity
 * @param requestCode used to handle [Fragment.onActivityResult]/[android.app.Activity.onActivityResult]
 * @param allowSyncWithGallery boolean to check if should copy captured image/video to the gallery
 * @param galleryFolderName for custom folder name to save camera/video files
 *by default null and captured files saved in default folders
 *@param selectionMode refers to [SelectionMode] for [Intent.ACTION_OPEN_DOCUMENT] selection mode and
 *by default is single selection
 * */
class PickFilesFactory(
    private val caller: Any,
    private val requestCode: Int,
    private val allowSyncWithGallery: Boolean = false,
    private val galleryFolderName: String? = null,
    private val selectionMode: SelectionMode = SelectionMode.SINGLE
) {

    /**
     * @return instance of [IPickFilesFactory] implementation to handle file picking
     * @param fileTypes refers to [FileTypes] check types and return instance
     * [FileTypes.CAPTURE_IMAGE] to handle capture images
     * [FileTypes.CAPTURE_VIDEO] to handle record videos
     * [FileTypes.PICK_FILES] to handel open documents and pick files
     * */
    fun getInstance(fileTypes: FileTypes): IPickFilesFactory? {
        val caller = Caller.getInstance(caller)
        return when (fileTypes) {
            FileTypes.CAPTURE_IMAGE ->
                CaptureImage(caller, requestCode, allowSyncWithGallery, galleryFolderName)
            FileTypes.CAPTURE_VIDEO ->
                CaptureVideo(caller, requestCode, allowSyncWithGallery, galleryFolderName)
            FileTypes.PICK_FILES ->
                PickFiles(caller, requestCode, selectionMode)
        }
    }
}