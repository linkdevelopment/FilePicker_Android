package com.linkdev.filepicker_android.pickFilesComponent.model

import java.lang.Exception

enum class MimeType(val mimeTypeName: String, val mimeTypeExtension: Set<String>) {
    // ============== images ==============
    JPEG("image/jpeg", setOf("jpg")),
    PNG("image/png", setOf("png")),
    GIF("image/gif", setOf("gif")),
    BMP("image/x-ms-bmp", setOf("bmp")),
    WEBP("image/webp", setOf("webp")),
    ALL("image/*", setOf("jpg", "jpeg", "png", "gif", "bmp", "webp"));

    companion object {
        fun getMimeTypeNames(mimeTypeSet: Set<MimeType>): String {
            if (mimeTypeSet.isEmpty()) throw Exception("Mime Type Should not be empty")
            return if (mimeTypeSet.size == 1) {
                mimeTypeSet.first().mimeTypeName
            } else {
                getArrayOfMimeType(mimeTypeSet).joinToString(separator = ",")
            }
        }

        fun getArrayOfMimeType(mimeTypeSet: Set<MimeType>): Array<String> {
            val mimeTypeList = ArrayList<String>()
            for (mimeType: MimeType in mimeTypeSet) {
                mimeTypeList.add(mimeType.mimeTypeName)
            }
            return mimeTypeList.toTypedArray()
        }
    }
}