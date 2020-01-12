package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import android.content.Intent
import com.linkdev.filepicker_android.pickFilesComponent.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType

interface IPickFilesFactory {
    fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String)
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        shouldMakeDir: Boolean = false,
        callback: PickFilesResultCallback
    )
}