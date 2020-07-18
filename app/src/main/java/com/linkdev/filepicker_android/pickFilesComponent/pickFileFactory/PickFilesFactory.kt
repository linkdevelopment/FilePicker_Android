package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.allFiles.AllFiles
import com.linkdev.filepicker_android.pickFilesComponent.image.AndroidQCaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.image.CaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.model.FactoryFilesType
import com.linkdev.filepicker_android.pickFilesComponent.utils.Platform
import com.linkdev.filepicker_android.pickFilesComponent.video.AndroidQCaptureVideo
import com.linkdev.filepicker_android.pickFilesComponent.video.CaptureVideo

class PickFilesFactory(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val shouldMakeDir: Boolean = false,
    private val contentProviderName: String? = null
) {

    fun getPickInstance(fileType: FactoryFilesType): IPickFilesFactory? {
        return when (fileType) {
            FactoryFilesType.IMAGE_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureImage(fragment, requestCode, shouldMakeDir)
                else
                    CaptureImage(fragment, requestCode, shouldMakeDir, contentProviderName)
            }
            FactoryFilesType.VIDEO_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureVideo(fragment, requestCode, shouldMakeDir)
                else
                    CaptureVideo(fragment, requestCode, shouldMakeDir, contentProviderName)

            }
            FactoryFilesType.PICK_FILES -> AllFiles(fragment, requestCode)
        }
    }
}