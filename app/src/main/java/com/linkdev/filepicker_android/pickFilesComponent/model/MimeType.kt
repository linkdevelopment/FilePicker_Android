package com.linkdev.filepicker_android.pickFilesComponent.model

import android.webkit.MimeTypeMap
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
    ALL_VIDEOS("video/*", setOf("mpeg", "mp4", "3gpp", "3gpp2", "avi")),

    // ============== TEXT FILES ==============
    TXT("text/plain", setOf("plain")),
    HTM("text/plain", setOf("html")),
    PDF("application/pdf", setOf("pdf")),
    WORD("application/msword", setOf("msword")),
    EXCEL("application/vnd.ms-excel", setOf("vnd.ms-excel")),

    // ============== Audio ==============
    MP3("audio/mpeg", setOf("mpeg")),
    ALL_AUDIO("audio/*", setOf("mpeg")),


    ALL_FILES("*/*", setOf("*/*"));

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