package com.linkdev.filepicker.interactions

import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData

interface PickFilesStatusCallback {
    fun onPickFileCanceled()
    fun onPickFileError(errorModel: ErrorModel)
    fun onFilePicked(fileData: ArrayList<FileData>)
}