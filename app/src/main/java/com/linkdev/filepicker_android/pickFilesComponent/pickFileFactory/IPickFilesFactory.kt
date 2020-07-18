package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import android.content.Intent
import com.linkdev.filepicker_android.pickFilesComponent.utils.PickFilesResultCallback
import com.linkdev.filepicker_android.pickFilesComponent.model.MimeType

interface IPickFilesFactory {
    fun pickFiles(mimeTypeSet: Set<MimeType>, chooserMessage: String)

    fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesResultCallback
    )
}