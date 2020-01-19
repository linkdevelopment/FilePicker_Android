package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.audio.PickAudio
import com.linkdev.filepicker_android.pickFilesComponent.image.CaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.image.PickGalleryImage
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType
import com.linkdev.filepicker_android.pickFilesComponent.textFiles.PickTextFiles
import com.linkdev.filepicker_android.pickFilesComponent.video.CaptureVideo
import com.linkdev.filepicker_android.pickFilesComponent.video.PickGalleryVideo

class PickFilesFactory(
    private val fragment: Fragment,
    private val shouldMakeDir: Boolean = false,
    private val contentProviderName: String = ""
) {

    fun getPickInstance(fileType: FilesType): IPickFilesFactory? {
        return when (fileType) {
            FilesType.IMAGE_GALLERY -> PickGalleryImage(fragment)
            FilesType.IMAGE_CAMERA -> CaptureImage(fragment, shouldMakeDir, contentProviderName)
            FilesType.VIDEO_GALLERY -> PickGalleryVideo(fragment)
            FilesType.VIDEO_CAMERA -> CaptureVideo(fragment, shouldMakeDir, contentProviderName)
            FilesType.TEXT_FILE -> PickTextFiles(fragment)
            FilesType.AUDIO_FILE -> PickAudio(fragment)
        }
    }
}