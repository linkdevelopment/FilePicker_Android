package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import android.content.Intent
import com.linkdev.filepicker_android.pickFilesComponent.interactions.PickFilesStatusCallback
import com.linkdev.filepicker_android.pickFilesComponent.models.MimeType

interface IPickFilesFactory {
    fun pickFiles(mimeTypeList: ArrayList<MimeType>, chooserMessage: String)

    fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    )
}