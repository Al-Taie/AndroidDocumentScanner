package com.scanner.library

import android.graphics.Bitmap


internal class NativeScanner {
    external fun getScannedBitmap(
        bitmap: Bitmap?,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        x4: Float,
        y4: Float
    ): Bitmap?

    external fun getGrayBitmap(bitmap: Bitmap?): Bitmap?

    external fun getMagicColorBitmap(bitmap: Bitmap?): Bitmap?

    external fun getBWBitmap(bitmap: Bitmap?): Bitmap?

    external fun getPoints(bitmap: Bitmap?): FloatArray?

    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("native_scanner")
        }
    }
}
