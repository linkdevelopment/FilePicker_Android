package com.linkdev.filepicker.utils

import android.util.Log

object LoggerUtils {
    const val TAG = "FilePickerTag"
    fun logError(message: String, cause: Throwable?) {
        Log.e(TAG, message, cause)
    }
}