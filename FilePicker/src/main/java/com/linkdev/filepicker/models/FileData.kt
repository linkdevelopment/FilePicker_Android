/*
 * Copyright (C) 2020 Link Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkdev.filepicker.models

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.util.Size
import androidx.annotation.Keep
import com.linkdev.filepicker.utils.file.FileUtils
import kotlinx.android.parcel.Parcelize
import java.io.File

/**
 * Data class to hold the information about captured image/video or picked file from the document library.
 * @param uri file Content URI
 * @param filePath in case pick file from the document library represents files in the cache subdirectory
 * of your app's internal storage area, the value returned by [android.content.Context.getCacheDir].
 * in case capture image/video represents files in the root of your app's external storage area,
 * the value returned by [android.content.Context.getExternalFilesDir].
 * @param file captured/captured file
 * @param fileName captured/picked file name
 * @param mimeType the mime type of the file e.g image/jpeg, video/mp4, application/pdf
 * @param fileSize captured/picked file size in bytes
 *  */
@Parcelize
@Keep
data class FileData(
    val uri: Uri? = null,
    val filePath: String? = null,
    val file: File? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val fileSize: Double? = null
) : Parcelable {

    /** Returns [Bitmap] thumbnail if selected file is Image or Video and return null if not.
     * @param context caller Activity/Fragment context
     * @param thumbnailSize desired size of thumbnail
     * */
    fun getThumbnail(context: Context, thumbnailSize: Size): Bitmap? {
        return when {
            uri == null || mimeType == null -> null
            MimeType.isImage(mimeType) ->
                FileUtils.getImageThumbnail(context, uri, thumbnailSize)
            MimeType.isVideo(mimeType) ->
                FileUtils.getVideoThumbnail(context, uri, thumbnailSize)
            else -> null
        }
    }
}