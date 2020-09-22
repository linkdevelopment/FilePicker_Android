package com.linkdev.filepicker_android.pickFilesComponent.interactions

import com.linkdev.filepicker_android.pickFilesComponent.models.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.models.FileData

interface PickFilesStatusCallback {
    fun onPickFileCanceled()
    fun onPickFileError(errorModel: ErrorModel)
    fun onFilePicked(fileData: ArrayList<FileData>)
}