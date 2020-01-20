package com.linkdev.filepicker_android.pickFilesComponent.utils

import android.os.Build

object Platform {
    fun isAndroidQ(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


    fun isAndroidN(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}