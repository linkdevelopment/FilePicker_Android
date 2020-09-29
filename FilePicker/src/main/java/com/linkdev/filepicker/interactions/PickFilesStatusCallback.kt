package com.linkdev.filepicker.interactions

import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData

/**
 * [onPickFileCanceled] when user cancel picking files from document, capture photo or record video
 * [onPickFileError] when some error occurred while picking files
 * [onFilePicked] when files picked successfully and get list of picked files*/
interface PickFilesStatusCallback {
    fun onPickFileCanceled()
    fun onPickFileError(errorModel: ErrorModel)
    fun onFilePicked(fileData: ArrayList<FileData>)
}