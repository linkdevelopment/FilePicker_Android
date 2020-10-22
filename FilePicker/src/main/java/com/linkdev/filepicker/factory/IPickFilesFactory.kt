/*
 * Copyright (C) 2020 Link Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkdev.filepicker.factory

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.linkdev.filepicker.interactions.PickFilesStatusCallback
import com.linkdev.filepicker.models.MimeType

interface IPickFilesFactory {
    /**
     * implemented by each class to handle Intent action
     * @param mimeTypeList is list of mime Types to be allowed*/
    fun pickFiles(mimeTypeList: ArrayList<MimeType> = arrayListOf(MimeType.ALL_FILES))

    /**
     * used to handle Activity result called on the host view [Fragment.onActivityResult]/[Activity.onActivityResult]
     * @param mRequestCode to identify who this result came from
     * @param resultCode to identify if operation succeeded or canceled
     * @param data return result data to the caller
     * @param callback handle file status
     */
    fun handleActivityResult(
        mRequestCode: Int, resultCode: Int, data: Intent?, callback: PickFilesStatusCallback
    )
}