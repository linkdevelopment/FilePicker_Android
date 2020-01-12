package com.linkdev.filepicker_android.pickFilesComponent

import android.net.Uri
import java.io.File

interface PickFilesResultCallback {
    fun onPickFileCanceled()
    fun onPickFileError()
    fun onFilePicked(uri: Uri, filePath: String?, file: File?)
}