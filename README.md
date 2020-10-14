# **FilePicker**
[![Platform](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html)
![API](https://img.shields.io/badge/Min--SDK-21-yellowgreen)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
# **What is it?**
FilePicker allows you easily capture image, record video from camera or pick any type of files from document with custom Mime types without creating lots of boilerplate.

# **Setup**

### To Import FilePicker to your project:
1. Open your project in Android Studio
2. Download the library (using Git, or a zip archive to unzip)
3. Go to File > Import Module and import the library as a module
4. Right-click on app in your project view and select "Open Module Settings"
5. Click the "Dependencies" tab and then the '+' button
6. Select "Module Dependency"
7. Select "FilePicker"

### Runtime permissions
This library requires specific runtime permissions. Declare it in your `AndroidMnifest.xml`:

For capture image or record video:
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```
For pick any type of files from document:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
**Note**: for devices running API 23 (Marshmallow) and up you have to request these permissions in the runtime, before calling `IPickFilesFactory.pickFiles()`. It's demonstrated in the sample app.

# **Usage**

Create IPickFileFactory instance like this:
```kotlin
private var pickFilesFactory: IPickFilesFactory? = null
```
### Capture photo

To capture image we need to get instance of PickFilesFactory by passing FileTypes.IMAGE_CAMERA to getInstance() method.

```kotlin
pickFilesFactory = PickFilesFactory(
               caller: Any,
               requestCode: Int,
               galleryFolderName: String? = null,
               allowSyncWithGallery: Boolean? = false,
               thumbnailSize: Size
            ).getInstance(FileTypes.IMAGE_CAMERA)
```
To open camera by MediaStore.ACTION_IMAGE_CAPTURE and create image URI, need to call pickFilesFactory?.pickFiles()
```kotlin
pickFilesFactory?.pickFiles()
```

### Record video

To record video we need to get instance of PickFilesFactory by passing FileTypes.VIDEO_CAMERA to getInstance() method.
```kotlin
pickFilesFactory = PickFilesFactory(
               caller: Any,
               requestCode: Int,
               galleryFolderName: String? = null,
               allowSyncWithGallery: Boolean? = false,
               thumbnailSize: Size
            ).getInstance(FileTypes.VIDEO_CAMERA)
            pickFilesFactory?.pickFiles()
```
To open camera by MediaStore.ACTION_VIDEO_CAPTURE and create video URI, need to call pickFilesFactory?.pickFiles()
```kotlin
pickFilesFactory?.pickFiles()
```
### pick files from documents
To pick any type of file from document, we need to get instance of PickFilesFactory by passing FileTypes.IMAGE_CAMERA to getInstance() method.

```kotlin
pickFilesFactory = PickFilesFactory(
               caller: Any,
               requestCode: Int,
               selectionMode: SelectionMode = SelectionMode.SINGLE,
               thumbnailSize: Size
            ).getInstance(FileTypes.PICK_FILES)
```
To open document by Intent.ACTION_OPEN_DOCUMENT, need to call pickFilesFactory?.pickFiles() and pass mime type list if need to allow specific mime types
```
pickFilesFactory?.pickFiles(mimeTypeList: ArrayList<MimeType> = arrayListOf(MimeType.ALL_FILES))
```
### PickFilesFactory attributes
**caller**
```
refers to host Fragment/Activity. Used to get context and startActivityForResult
```
**requestCode**
```
used to handle [Fragment.onActivityResult]/[android.app.Activity.onActivityResult]
```
**allowSyncWithGallery**
```
check if should copy captured image/video to the gallery
```
**galleryFolderName**
```
app specific folder name in gallery by default is null and captured image/video saved in default folder
```
**selectionMode**
```
refers to [SelectionMode] enum class. Used to check if should allow multiple selection or single selection
```
**thumbnailSize**
 ```
 refers to [Size] class. used for thumbnail custom size
 ```
  
```
### Getting selected files list
```kotlin
fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFilesFactory?.handleActivityResult(requestCode, resultCode, data, object :
        PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            // pick file process canceled by user (resultCode = RESULT_CANCELED)
        }

        override fun onPickFileError(errorModel: ErrorModel) {
            // some error occurred
        }

        override fun onFilePicked(fileData: ArrayList<FileData>) {
            // files picked successfully 
        }
    })
                
 }
```
#### FileData.kt
```
Is a data class containing captured/picked file information
```
**uri:**
```
file saved uri.
```
**tempFilePath:**
```
file path in storage
```
**tempFile:**
```
captured / picked file
```

**fileName:**
```
captured / picked file name
```

**mimeType:**
```
captured / picked file mime type
```

**fileSize:**
```
captured / picked file size in bytes
```

**thumbnail:**
```
thumbnail bitmap for captured/selected image/video
```
## Enums
#### MimeType.kt
```
Is an enum class containing all possible mime types used when pick files from document. usage documented in the sample app.  
```
#### FileTypes.kt
```
Is an enum class containing three types CAPTURE_IMAGE, CAPTURE_VIDEO and PICK_FILES used to get instance of IPickFilesFactory based on usage:
CAPTURE_IMAGE: passed when need to captured image. 
CAPTURE_VIDEO: passed when need to record video. 
PICK_FILES: passed when need to pick files from document.
usage documented in sample app 
```
#### SelectionMode.kt
```
Is an enum class containing two types SINGLE, MULTIPLE used to detect if should allow multiple selection from document:
SINGLE: will not allow multiple selection
MULTIPLE: will allow multiple selection
usage documented in sample app 
```
# **License**
    Copyright 2020-present Link Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 