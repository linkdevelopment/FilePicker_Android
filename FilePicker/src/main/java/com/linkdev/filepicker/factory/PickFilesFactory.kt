package com.linkdev.filepicker.factory

import androidx.fragment.app.Fragment
import com.linkdev.filepicker.image.AndroidQCaptureImage
import com.linkdev.filepicker.image.CaptureImage
import com.linkdev.filepicker.models.FactoryFilesType
import com.linkdev.filepicker.models.SelectionTypes
import com.linkdev.filepicker.pick_files.PickFiles
import com.linkdev.filepicker.utils.Platform
import com.linkdev.filepicker.video.AndroidQCaptureVideo
import com.linkdev.filepicker.video.CaptureVideo

class PickFilesFactory(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val folderName: String? = null,
    private val contentProviderName: String? = null,
    private val selectionType: SelectionTypes = SelectionTypes.SINGLE
) {

    fun getPickInstance(fileType: FactoryFilesType): IPickFilesFactory? {
        return when (fileType) {
            FactoryFilesType.IMAGE_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureImage(fragment, requestCode, folderName)
                else
                    CaptureImage(fragment, requestCode, folderName, contentProviderName)
            }
            FactoryFilesType.VIDEO_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureVideo(fragment, requestCode, folderName)
                else
                    CaptureVideo(fragment, requestCode, folderName, contentProviderName)

            }
            FactoryFilesType.PICK_FILES -> PickFiles(fragment, requestCode, selectionType)
        }
    }
}