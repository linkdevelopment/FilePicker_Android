package com.linkdev.filepicker_android.pickFilesComponent.model

import java.lang.Exception

enum class MimeType(val mimeTypeName: String, val mimeTypeExtension: Set<String>) {
    // ============== IMAGES ==============
    JPEG("image/jpeg", setOf("jpg")),
    PNG("image/png", setOf("png")),
    GIF("image/gif", setOf("gif")),
    BMP("image/x-ms-bmp", setOf("bmp")),
    WEBP("image/webp", setOf("webp")),
    ALL_IMAGES("image/*", setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")),

    // ============== VIDEOS ==============
    MPEG("video/mpeg", setOf("mpeg")),
    MP4("video/mp4", setOf("mp4")),
    GPP("video/3gpp", setOf("3gpp")),
    GPP2("video/3gpp2", setOf("3gpp2")),
    AVI("video/avi", setOf("avi")),
    ALL_VIDEOS("video/*", setOf("mpeg", "mp4", "3gpp", "3gpp2", "avi"));

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