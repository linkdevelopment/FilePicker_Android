package com.linkdev.filepicker_android.pickFilesComponent.model

import java.lang.Exception

enum class MimeType(val mimeTypeName: String, val mimeTypeExtension: ArrayList<String>) {
    // ============== IMAGES ==============
    JPEG("image/jpeg", arrayListOf("jpg")),
    PNG("image/png", arrayListOf("png")),
    GIF("image/gif", arrayListOf("gif")),
    BMP("image/x-ms-bmp", arrayListOf("bmp")),
    WEBP("image/webp", arrayListOf("webp")),
    ALL_IMAGES("image/*", arrayListOf("jpg", "jpeg", "png", "gif", "bmp", "webp")),

    // ============== VIDEOS ==============
    MPEG("video/mpeg", arrayListOf("mpeg")),
    MP4("video/mp4", arrayListOf("mp4")),
    GPP("video/3gpp", arrayListOf("3gpp")),
    GPP2("video/3gpp2", arrayListOf("3gpp2")),
    AVI("video/avi", arrayListOf("avi")),
    ALL_VIDEOS("video/*", arrayListOf("mpeg", "mp4", "3gpp", "3gpp2", "avi")),

    // ============== TEXT FILES ==============
    TXT("text/plain", arrayListOf("txt", "tex")),
    PDF("application/pdf", arrayListOf("pdf")),
    WORD("application/msword", arrayListOf("doc", "docx")),
    EXCEL("application/vnd.ms-excel", arrayListOf("xls", "xlsx")),

    // ============== Audio ==============
    MP3("audio/mpeg", arrayListOf("mpeg", "mp3")),
    ALL_AUDIO("audio/*", arrayListOf("mpeg", "mp3")),


    ALL_FILES("*/*", arrayListOf("*/*"));

    companion object {
        fun getArrayOfMimeType(mimeTypeList: ArrayList<MimeType>): Array<String> {
            if (mimeTypeList.isEmpty()) throw Exception("File Picker Error, MIME type cannot be empty")
            val mMimeTypeList = ArrayList<String>()
            for (mimeType: MimeType in mimeTypeList) {
                mMimeTypeList.add(mimeType.mimeTypeName)
            }
            return mMimeTypeList.toTypedArray()
        }
    }
}