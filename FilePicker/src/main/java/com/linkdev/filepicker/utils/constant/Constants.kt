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

package com.linkdev.filepicker.utils.constant

internal object Constants {
    object ErrorMessages {
        const val NOT_HANDLED_DOCUMENT_ERROR_MESSAGE =
            "Please handle android.permission.READ_EXTERNAL_STORAGE runtime permission"
        const val NOT_HANDLED_CAMERA_ERROR_MESSAGE =
            "Please handle android.permission.CAMERA and android.permission.WRITE_EXTERNAL_STORAGE runtime permission"
        const val NO_CAMERA_HARDWARE_AVAILABLE_ERROR_MESSAGE =
            "No camera found to process this operation to avoid devices without camera to install your app, please add \n" +
                    "<uses-feature\n" +
                    "        android:name=\"android.hardware.camera\"\n" +
                    "        android:required=\"true\" /> \n" +
                    "to your manifest"
        const val REQUEST_CODE_ERROR_MESSAGE = "Incorrect request code"
    }
}