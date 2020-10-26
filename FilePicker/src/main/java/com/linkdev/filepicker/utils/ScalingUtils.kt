package com.linkdev.filepicker.utils

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
            inJustDecodeBounds = false
            inSampleSize =
                calculateSampleSize(outWidth, outHeight, dstWidth, dstHeight)
        }
        return BitmapFactory.decodeFile(path, options)
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