package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.allFiles.AllFiles
import com.linkdev.filepicker_android.pickFilesComponent.image.AndroidQCaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.image.CaptureImage
import com.linkdev.filepicker_android.pickFilesComponent.models.FactoryFilesType
import com.linkdev.filepicker_android.pickFilesComponent.utils.Platform
import com.linkdev.filepicker_android.pickFilesComponent.video.AndroidQCaptureVideo
import com.linkdev.filepicker_android.pickFilesComponent.video.CaptureVideo

class PickFilesFactory(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val shouldMakeDir: Boolean = false,
    private val folderName: String? = null,
    private val contentProviderName: String? = null
) {

    fun getPickInstance(fileType: FactoryFilesType): IPickFilesFactory? {
        return when (fileType) {
            FactoryFilesType.IMAGE_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureImage(fragment, requestCode, shouldMakeDir, folderName)
                else
                    CaptureImage(fragment, requestCode, shouldMakeDir,folderName, contentProviderName)
            }
            FactoryFilesType.VIDEO_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureVideo(fragment, requestCode, shouldMakeDir, folderName)
                else
                    CaptureVideo(fragment, requestCode, shouldMakeDir,folderName, contentProviderName)

            }
            FactoryFilesType.PICK_FILES -> AllFiles(fragment, requestCode)
        }
    }
}