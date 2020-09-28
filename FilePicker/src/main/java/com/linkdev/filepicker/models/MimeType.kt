package com.linkdev.filepicker.models

import java.lang.Exception

enum class MimeType(val mimeTypeName: String, val mimeTypeExtension: ArrayList<String>) {
    // ============== IMAGES ==============
    JPEG("image/jpeg", arrayListOf("jpg", "jpeg")),
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
    DOC("application/msword", arrayListOf("doc", "docx")),
    DOCX(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        arrayListOf("docx")
    ),
    XLS("application/vnd.ms-excel", arrayListOf("xls")),
    XLSX(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        arrayListOf("xlsx")
    ),
    PTT("application/vnd.ms-powerpoint", arrayListOf(".ppt")),
    PTTX(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        arrayListOf(".pptx")
    ),

    // ============== Audio ==============
    MP3("audio/mpeg", arrayListOf("mpeg", "mp3")),
    ALL_AUDIO("audio/*", arrayListOf("mpeg", "mp3")),


    ALL_FILES("*/*", arrayListOf("*/*"));

    companion object {
        fun getArrayOfMimeType(mimeTypeList: ArrayList<MimeType>): Array<String> {
            if (mimeTypeList.isEmpty()) return arrayOf(ALL_FILES.mimeTypeName)
            val mMimeTypeList = ArrayList<String>()
            for (mimeType: MimeType in mimeTypeList) {
                mMimeTypeList.add(mimeType.mimeTypeName)
            }
            return mMimeTypeList.toTypedArray()
        }

        fun toList(): ArrayList<MimeType> {
            return arrayListOf(
                ALL_IMAGES, JPEG, PNG, GIF, BMP, WEBP, ALL_VIDEOS, MPEG,
                MP4, GPP, GPP2, AVI, TXT, PDF, DOC, DOCX, XLS, XLSX, PTT,
                PTTX
            )
        }
    }
}