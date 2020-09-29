package com.linkdev.filepicker.utils

import android.os.Build

internal object Platform {
    fun isAndroidQ(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


    fun isAndroidN(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}