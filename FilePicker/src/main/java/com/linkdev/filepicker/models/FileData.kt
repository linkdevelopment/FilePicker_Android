package com.linkdev.filepicker.models

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
@Keep
data class FileData(
    val uri: Uri? = null,
    val filePath: String? = null,
    val file: File? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val intent: Intent? = null
) : Parcelable