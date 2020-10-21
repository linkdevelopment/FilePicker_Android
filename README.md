# **FilePicker**
[![Platform](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html)
![API](https://img.shields.io/badge/Min--SDK-21-yellowgreen)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

FilePicker allows you to easily capture an image, record a video or pick any file from the document library.

![](images/file_picker_sample_1.gif)
![](images/file_picker_sample_2.gif)
![](images/file_picker_sample_3.gif)

# **Setup**

### To Import FilePicker to your project:
1. Open your project in Android Studio
2. Download the library (using Git, or a zip archive to unzip)
3. Go to File > Import-Module and import the library as a module
4. Right-click on the app in your project view and select "Open Module Settings"
5. Click the "Dependencies" tab and then the '+' button
6. Select "Module Dependency"
7. Select "FilePicker"

### Runtime permissions
This library requires specific runtime permissions. Declare it in your `AndroidMnifest.xml`:

For capturing an image or recording a video:
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```
For picking files from the document library:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
**Note**: for devices running API 23 (Marshmallow) and up you have to request these permissions in the runtime, before calling `IPickFilesFactory.pickFiles()`. It's demonstrated in the sample app.

# **Usage**

Create IPickFileFactory instance:
```kotlin
private var pickFilesFactory: IPickFilesFactory? = null
```
### Capture photo

To capture images we need to get an instance of PickFilesFactory by passing FileTypes.CAPTURE_IMAGE to getInstance() method.

```kotlin
pickFilesFactory = PickFilesFactory(
                caller = this,
                requestCode = 1001,
                galleryFolderName = "File Picker_Images",
                allowSyncWithGallery = true
            ).getInstance(FileTypes.CAPTURE_IMAGE)
```
To start capturing images, call
```kotlin
pickFilesFactory?.pickFiles()
```

### Record video

To record video we need to get instance of PickFilesFactory by passing FileTypes.CAPTURE_VIDEO to getInstance() method.
```kotlin
pickFilesFactory = PickFilesFactory(
                caller = this,
                requestCode = 1003,
                galleryFolderName = "File Picker_Videos",
                allowSyncWithGallery = true
            ).getInstance(FileTypes.CAPTURE_VIDEO)
```
To start capturing videos, call
```kotlin
pickFilesFactory?.pickFiles()
```

**Note** Please add below lines to your manifest file if an essential function of your application is taking pictures,  
 then restrict its visibility on Google Play to devices that have a camera. If accidentally forget to add <uses-feature..>
 and device without camera try to capture image/video library will throw RuntimeException.
```xml
<uses-feature
        android:name="android.hardware.camera"
        android:required="true" />
```
### pick files from documents
To pick any type of file from a document, we need to get an instance of PickFilesFactory by passing FileTypes.PICK_FILES to getInstance() method.

```kotlin
pickFilesFactory = PickFilesFactory(
                caller = this,
                requestCode = 1004,
                selectionMode = SelectionMode.MULTIPLE
            ).getInstance(fileTypes = FileTypes.PICK_FILES)
```
To open the document by Intent.ACTION_OPEN_DOCUMENT, need to call pickFilesFactory?.pickFiles() and pass the mime types to allow specific file types
```kotlin
pickFilesFactory?.pickFiles(mimeTypeList = arrayListOf(MimeType.ALL_FILES))
```
### PickFilesFactory
Class used to provide instance of each pick file type.

**params**:
* `caller` {Any}: host Fragment/Activity. Used to get context and to call startActivityForResult
* `requestCode` {Int}: handle host Fragment/Activity onActivityResult
* `allowSyncWithGallery` {Boolean}: Set to true, if you would like the captured images and videos to be added to the Gallery.
* `galleryFolderName` {@Nullable String}: Set it to a name if you would like the captured images and videos to be added to the Gallery inside a folder specific to your app. If not sent, the files will be saved in the default folder.
* `selectionMode` {SelectionMode}: In case of picking files, you can set the selection mode to be single or multiple.

**SelectionMode.kt**

Is an enum class  used to detect if should allow multiple selections from the document:

* `SINGLE`: Select if you need to allow multiple selections
* `MULTIPLE`: Select if you need to allow multiple selections


**FileTypes.kt**

Is an enum class used to get an instance of IPickFilesFactory based on usage:

* `CAPTURE_IMAGE`: Select if you need to capture an image.
* `CAPTURE_VIDEO`: Select if you need to record a video.
* `PICK_FILES`: Select if you need to pick files from the document.

**MimeType.kt**

Is an enum class containing all possible mime types used when picking files from the document. usage documented in the sample app.

<table>
  <thead>
    <tr><th align="center">Images</th><th align="center">Videos</th><th align="center">Text files</th><th align="center">Audio</th><th align="center">All files</th></tr>
  </thead>
  <tbody>
    <tr><td align="center"> &quot;image/jpeg&quot;, &quot;image/png&quot;, &quot;image/gif&quot;, &quot;image/x-ms-bmp&quot;, &quot;image/webp&quot;, &quot;image/*&quot; </td><td align="center"> &quot;video/mpeg&quot;, &quot;video/mp4&quot;, &quot;video/3gpp&quot;, &quot;video/3gpp2&quot;, &quot;video/avi&quot;, &quot;video/*&quot; </td><td align="center"> &quot;text/plain&quot;, &quot;application/pdf&quot;, &quot;application/msword&quot;, &quot;application/vnd.openxmlformats-officedocument.wordprocessingml.document&quot;, &quot;application/vnd.ms-excel&quot;, &quot;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet&quot;, &quot;application/vnd.ms-powerpoint&quot;, &quot;application/vnd.openxmlformats-officedocument.presentationml.presentation&quot; </td><td align="center"> &quot;audio/mpeg&quot;, &quot;audio/3gpp&quot;, &quot;audio/mp4&quot;, &quot;audio/amr&quot;, &quot;audio/midi&quot;, &quot;audio/x-midi&quot;, &quot;application/ogg&quot;, &quot;audio/wav&quot;, &quot;audio/*&quot; </td><td align="center"> &quot;*/* &quot;  </td></tr>
  </tbody>
</table>


### Getting selected files list
In your caller Activity/Fragment you need to call **pickFilesFactory?.handleActivityResult()** inside the Activity's or the fragment's onActivityResult callback to be able to get the list of selected files by passing an instance of the PickFilesStatusCallback interface .
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

### PickFilesStatusCallback
An interface to handle captured/picked file status as action canceled, some error occurred or files picked successfully.

### Methods
### onPickFileCanceled
A method fired to be informed that the action is canceled.

**Example**:
```kotlin
object : PickFilesStatusCallback {
        override fun onPickFileCanceled() {
            Toast.makeText(requireContext(), "Action canceled", Toast.LENGTH_SHORT).show()
            // show toast with friendly message, includes that action has been canceled
        }
        ...
    }
```
### onPickFileError
A method fired to be informed that an error occurred.

**Params**:
* `errorModel` {data class}: Data class hold ErrorStatus that describing the type of error occurred, and friendly message that

**Example**:
```kotlin
object : PickFilesStatusCallback {
        ...
        override fun onPickFileError(errorModel: ErrorModel) {
            when (errorModel.errorStatus) {
                ErrorStatus.DATA_ERROR -> showToastMessage(errorModel.errorMessage)
                ErrorStatus.FILE_ERROR -> showToastMessage(errorModel.errorMessage)
                ErrorStatus.PICK_ERROR -> showToastMessage(errorModel.errorMessage)
            }
        }
        // check every error status and show friendly message describe the error.
        ...
     }
```
**ErrorModel.kt**

Data class hold ErrorStatus that describing the type of error occurred, and friendly message that

**params**:
* `errorStatus` {ErrorStatus}: Enum class describing the type of error
* `errorMessage` {Int}: String resource friendly error message

**ErrorStatus.kt**

An enum describing the type of error occurred.

* `DATA_ERROR`: refers to some required data (file,mime type,..etc) is corrupted.
* `FILE_ERROR`: refers to error occurred while capturing file and/or save the file
* `PICK_ERROR`: refers to data retrieved is null or empty while picking files from document

### onFilePicked
A method fired to be informed that data retrieved successfully.

**Params**:
* `fileData` {ArrayList}: A list of files.

**Example**:
```kotlin
object : PickFilesStatusCallback {
        ...
        override fun onFilePicked(fileData: ArrayList<FileData>) {
            attachedFilesAdapter.replaceFiles(fileData)
            // show selected files into recycler view
        }
    }
```

**FileData.kt**

Data class to hold the information about captured image/video or picked file from the document library.

**params**:

* `uri` {Uri}: File Content URI
* `filePath` {String}: in case of picking file from the document library, this attribute will hold
the file path in the cache subdirectory of your app's internal storage area,
the value returned by android.content.Context.getCacheDir.
in case of capturing image/video, this attribute will hold the file's path in the root of your app's external storage area,
the value returned by android.content.Context.getExternalFilesDir
* `file` {File}: Captured/picked file
* `fileName` {String}: Captured/picked file name
* `mimeType` {String}: The mime type of the file e.g image/jpeg, video/mp4, application/pdf
* `fileSize` {Double}: Captured/picked file size in bytes

**Methods**


**.getThumbnail**

Call this method, if you would like to get a thumbnail image for the captured image or video

**Params**:

* `context` {Context}: Caller Activity/Fragment context.
* `thumbnailSize` {Size}: Desired size of the thumbnail
* `return` {Bitmap}: Resized bitmap or null

**Example**:
```kotlin
 val thumbnail = fileData.getThumbnail(context, Size(200, 200))
 imgThumbnail.setImageBitmap(thumbnail)
 
// get thumbnail with desired size and set it to image view.
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
 