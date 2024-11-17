package com.scanner.library

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.scanner.library.utils.isValid
import com.scanner.library.utils.toOffsetPoints


class DocumentScanner {
    private val scanner by lazy { NativeScanner() }

    fun configureScanner(
        filterEnabled: Boolean = true,
        applyCLAHE: Boolean = true,
        scaleFactor: Double = 2.0,
        contrastValue: Double = 1.15,
        contrastLimitThreshold: Double = 1.5
    ) = scanner.configureScanner(
        filterEnabled = filterEnabled,
        applyCLAHE = applyCLAHE,
        scaleFactor = scaleFactor,
        contrastValue = contrastValue,
        contrastLimitThreshold = contrastLimitThreshold
    )

    fun getBestPoints(bitmap: Bitmap): List<Offset> = sequenceOf(
        { bitmap },
        ::getMagicColorBitmap,
        ::getBwBitmap,
        ::getGrayBitmap
    ).map { transformation -> getPoints(transformation(bitmap)) }
        .firstOrNull(List<Offset>::isValid) ?: emptyList()

    fun getBwBitmap(bitmap: Bitmap?): Bitmap? = scanner.getBwBitmap(bitmap)

    fun getGrayBitmap(bitmap: Bitmap?): Bitmap? = scanner.getGrayBitmap(bitmap)

    fun getMagicColorBitmap(bitmap: Bitmap?): Bitmap? = scanner.getMagicColorBitmap(bitmap)

    fun getPoints(bitmap: Bitmap?): List<Offset> = scanner.getPoints(bitmap).toOffsetPoints()

    fun getScannedBitmap(bitmap: Bitmap, points: List<Offset>): Bitmap? = runCatching {
        val point1 = points[0]
        val point2 = points[1]
        val point3 = points[2]
        val point4 = points[3]

        scanner.getScannedBitmap(
            bitmap = bitmap,
            x1 = point1.x,
            y1 = point1.y,
            x2 = point2.x,
            y2 = point2.y,
            x3 = point3.x,
            y3 = point3.y,
            x4 = point4.x,
            y4 = point4.y,
        )
    }.getOrNull()
}
