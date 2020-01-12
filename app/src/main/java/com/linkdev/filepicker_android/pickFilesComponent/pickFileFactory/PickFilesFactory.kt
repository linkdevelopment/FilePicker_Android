package com.linkdev.filepicker_android.pickFilesComponent.pickFileFactory

import androidx.fragment.app.Fragment
import com.linkdev.filepicker_android.pickFilesComponent.image.PickGalleryImage
import com.linkdev.filepicker_android.pickFilesComponent.model.FilesType

class PickFilesFactory(private val fragment: Fragment) {

    fun getPickInstance(fileType: FilesType): IPickFilesFactory? {
        return when (fileType) {
            FilesType.IMAGE_GALLERY -> PickGalleryImage(fragment)
            else -> null
        }
    }
}