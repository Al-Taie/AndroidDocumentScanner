package com.scanner.library.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size


fun ImageProxy.toRotatedBitmap() = toBitmap().rotate(imageInfo.rotationDegrees.toFloat())

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { setRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

internal fun FloatArray?.toOffsetPoints(): List<Offset> = runCatching {
    if (this == null) return emptyList()
    val halfSize = size / 2
    val points = mutableListOf<Offset>()
    for (indexOfX in 0 until halfSize) {
        val indexOfY = indexOfX + halfSize
        points.add(Offset(this[indexOfX], this[indexOfY]))
    }
    return points
}.getOrDefault(emptyList())

fun List<Offset>.isValid() = all(Offset::isValid) && size == 4 && all(Offset::isPositive)

fun Offset.isPositive() = x > 0 && y > 0

@Suppress("unusedReceiverParameter")
fun Any?.returnUnit() = Unit

val Bitmap.size: Size
    get() = Size(width.toFloat(), height.toFloat())

fun List<Offset>.getOrZero(index: Int): Offset = getOrNull(index) ?: Offset.Zero
