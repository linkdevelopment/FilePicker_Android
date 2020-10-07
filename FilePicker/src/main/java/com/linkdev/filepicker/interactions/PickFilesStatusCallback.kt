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

package com.linkdev.filepicker.interactions

import com.linkdev.filepicker.models.ErrorModel
import com.linkdev.filepicker.models.FileData

interface PickFilesStatusCallback {
    // used when user cancel picking files from document, capture photo or record video
    fun onPickFileCanceled()

    // used when an error occur while picking files
    fun onPickFileError(errorModel: ErrorModel)

    // used when files are picked successfully, will return the list of files*/
    fun onFilePicked(fileData: ArrayList<FileData>)
}