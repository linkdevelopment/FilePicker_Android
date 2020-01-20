package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.allFiles.AllFiles
import com.linkdev.filepicker_android.pickFilesComponent.audio.PickAudio
import com.linkdev.filepicker_android.pickFilesComponent.image.AndroidQCaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.image.CaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.image.PickGalleryImage
import com.linkdev.filepicker_android.pickFilesComponent.model.FactoryFilesType
import com.linkdev.filepicker_android.pickFilesComponent.textFiles.PickTextFiles
import com.linkdev.filepicker_android.pickFilesComponent.utils.Platform
import com.linkdev.filepicker_android.pickFilesComponent.video.AndroidQCaptureVideo
import com.linkdev.filepicker_android.pickFilesComponent.video.CaptureVideo
import com.linkdev.filepicker_android.pickFilesComponent.video.PickGalleryVideo

class PickFilesFactory(
    private val fragment: Fragment,
    private val shouldMakeDir: Boolean = false,
    private val contentProviderName: String? = null
) {

    fun getPickInstance(fileType: FactoryFilesType): IPickFilesFactory? {
        return when (fileType) {
            FactoryFilesType.IMAGE_GALLERY -> PickGalleryImage(fragment)
            FactoryFilesType.IMAGE_CAMERA -> {
                if (Platform.isAndroidQ()) AndroidQCaptureImage(fragment, shouldMakeDir)
                else CaptureImage(fragment, shouldMakeDir, contentProviderName)
            }
            FactoryFilesType.VIDEO_GALLERY -> PickGalleryVideo(fragment)
            FactoryFilesType.VIDEO_CAMERA -> {
                if (Platform.isAndroidQ()) AndroidQCaptureVideo(fragment, shouldMakeDir)
                else CaptureVideo(fragment, shouldMakeDir, contentProviderName)

            }
            FactoryFilesType.TEXT_FILE -> PickTextFiles(fragment)
            FactoryFilesType.AUDIO_FILE -> PickAudio(fragment)
            FactoryFilesType.ALL_FILES -> AllFiles(fragment)
        }
    }
}