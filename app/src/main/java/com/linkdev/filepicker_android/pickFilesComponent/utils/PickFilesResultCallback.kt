package com.linkdev.filepicker_android.pickFilesComponent.utils

import android.graphics.Bitmap
import android.net.Uri
import com.linkdev.filepicker_android.pickFilesComponent.model.DocumentFilesType
import com.linkdev.filepicker_android.pickFilesComponent.model.ErrorModel
import com.linkdev.filepicker_android.pickFilesComponent.model.FactoryFilesType
import java.io.File

interface PickFilesResultCallback {
    fun onPickFileCanceled()
    fun onPickFileError(errorModel: ErrorModel)
    fun onFilePicked(
        fileType: DocumentFilesType, uri: Uri?, filePath: String?, file: File?, bitmap: Bitmap?
    )
}