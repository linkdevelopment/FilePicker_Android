package com.linkdev.filepicker.models

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import java.io.File

data class FileData(
    val uri: Uri? = null,
    val filePath: String? = null,
    val file: File? = null,
    val bitmap: Bitmap? = null,
    val intent: Intent? = null
)