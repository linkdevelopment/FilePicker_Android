package com.linkdev.filepicker_android.pickFilesComponent.utils

import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FileData

interface PickFilesResultCallback {
    fun onPickFileCanceled()
    fun onPickFileError(errorModel: ErrorModel)
    fun onFilePicked(fileData: FileData)
}