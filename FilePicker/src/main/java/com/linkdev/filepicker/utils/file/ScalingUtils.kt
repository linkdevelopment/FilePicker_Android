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

package com.linkdev.filepicker.utils.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Class containing static utility methods for bitmap decoding and scaling
 */
object ScalingUtils {

    /**
     * Decode a file path into a sample bitmap
     * @param path file path to be decoded
     * @param dstWidth desired width
     * @param dstHeight desired height
     * @return The decoded bitmap, or null if the image data could not be decoded
     * */
    fun decodeFile(path: String?, dstWidth: Int, dstHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
        }
        return if (options.outWidth <= dstWidth || options.outHeight <= dstHeight) {
            BitmapFactory.decodeFile(path)
        } else {
            options.inJustDecodeBounds = false
            options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight)
            BitmapFactory.decodeFile(path, options)
        }
    }

    /**
     * Calculate optimal down-sampling factor given the dimensions of a source
     * image, the dimensions of a destination area and a scaling logic.
     *
     * @param srcWidth Width of source image
     * @param srcHeight Height of source image
     * @param dstWidth Width of destination area
     * @param dstHeight Height of destination area
     * @return Optimal down scaling sample size for decoding
     */
    private fun calculateSampleSize(
        srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int
    ): Int {
        var inSampleSize = 1
        if (srcHeight > dstHeight || srcWidth > dstWidth) {
            val halfHeight: Int = srcHeight / 2
            val halfWidth: Int = srcWidth / 2
            while (halfHeight / inSampleSize >= dstHeight && halfWidth / inSampleSize >= dstWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}