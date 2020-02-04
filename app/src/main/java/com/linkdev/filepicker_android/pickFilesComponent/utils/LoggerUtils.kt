package com.linkdev.filepicker_android.pickFilesComponent.utils

import android.util.Log

object LoggerUtils {
    const val TAG = "FilePickerTag"
    fun logError(message: String, cause: Throwable?) {
        Log.e(TAG, message, cause)
    }
}