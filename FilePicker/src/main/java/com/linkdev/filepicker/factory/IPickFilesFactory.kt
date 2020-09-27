package com.linkdev.filepicker.factory

import android.content.Intent
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.MimeType

interface IPickFilesFactory {
    fun pickFiles(mimeTypeList: ArrayList<MimeType> = arrayListOf(MimeType.ALL_FILES))

    fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    )
}