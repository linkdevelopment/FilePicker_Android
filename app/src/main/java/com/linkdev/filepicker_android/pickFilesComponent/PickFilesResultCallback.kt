package com.linkdev.filepicker_android.pickFilesComponent

import android.graphics.Bitmap
import android.net.Uri
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType
import java.io.File

interface PickFilesResultCallback {
    fun onPickFileCanceled()
    fun onPickFileError(errorModel: ErrorModel)
    fun onFilePicked(
        fileType: FilesType, uri: Uri?, filePath: String?, file: File?, bitmap: Bitmap?
    )
}