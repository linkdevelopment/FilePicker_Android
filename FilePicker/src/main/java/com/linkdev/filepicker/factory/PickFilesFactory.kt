package com.linkdev.filepicker.factory

import android.content.Intent
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.image.AndroidQCaptureImage
import com.linkdev.filepicker.image.CaptureImage
import com.linkdev.filepicker.models.FactoryFilesType
import com.linkdev.filepicker.models.SelectionTypes
import com.linkdev.filepicker.pick_files.PickFiles
import com.linkdev.filepicker.utils.Platform
import com.linkdev.filepicker.video.AndroidQCaptureVideo
import com.linkdev.filepicker.video.CaptureVideo

/**
 * Class use to provide instance of each pick file type by [getPickInstance] and check [FactoryFilesType]
 * @param fragment host fragment
 * @param requestCode used to handle [Fragment.onActivityResult]
 * @param folderName for custom folder name to save camera/video files
 *                   by default null and captured files saved in default folders
 *@param selectionType refers to [SelectionTypes] for [Intent.ACTION_OPEN_DOCUMENT] selection type and
 *                   by default is single selection
 * */
class PickFilesFactory(
    private val fragment: Fragment,
    private val requestCode: Int,
    private val folderName: String? = null,
    private val selectionType: SelectionTypes = SelectionTypes.SINGLE
) {

    /**
     * @return instance of [IPickFilesFactory] implementation to handle file picking
     * @param fileType refers to [FactoryFilesType] check types and return instance
     * [FactoryFilesType.IMAGE_CAMERA] to handle capture images
     * [FactoryFilesType.VIDEO_CAMERA] to handle record videos
     * [FactoryFilesType.PICK_FILES] to handel open documents and pick files
     * */
    fun getPickInstance(fileType: FactoryFilesType): IPickFilesFactory? {
        return when (fileType) {
            FactoryFilesType.IMAGE_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureImage(fragment, requestCode, folderName)
                else
                    CaptureImage(fragment, requestCode, folderName)
            }
            FactoryFilesType.VIDEO_CAMERA -> {
                if (Platform.isAndroidQ())
                    AndroidQCaptureVideo(fragment, requestCode, folderName)
                else
                    CaptureVideo(fragment, requestCode, folderName)

            }
            FactoryFilesType.PICK_FILES -> PickFiles(fragment, requestCode, selectionType)
        }
    }
}