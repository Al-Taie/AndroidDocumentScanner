package com.scanner.library.utils

import androidx.camera.view.PreviewView.ScaleType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path


internal fun ScaleType.calculateScaleFactor(
    viewSize: Size,
    imageSize: Size,
): Float = when (this) {
    ScaleType.FILL_CENTER -> {
        if (viewSize.aspectRatio > imageSize.aspectRatio)
            viewSize.width / imageSize.width
        else
            viewSize.height / imageSize.height
    }

    ScaleType.FIT_CENTER -> {
        if (viewSize.aspectRatio < imageSize.aspectRatio)
            viewSize.width / imageSize.width
        else
            viewSize.height / imageSize.height
    }

    else -> throw IllegalArgumentException("Unsupported ScaleType: $this")
}

internal fun ScaleType.calculatePostScaleOffset(
    viewSize: Size,
    imageSize: Size,
    scaleFactor: Float,
): Offset {
    val scaledWidth = imageSize.width * scaleFactor
    val scaledHeight = imageSize.height * scaleFactor
    return when (this) {
        ScaleType.FILL_CENTER -> {
            if (viewSize.aspectRatio < imageSize.aspectRatio)
                Offset.create(x = (scaledWidth - viewSize.width).half)
            else
                Offset.create(y = (scaledHeight - viewSize.height).half)
        }

        ScaleType.FIT_CENTER -> {
            if (viewSize.aspectRatio > imageSize.aspectRatio)
                Offset.create(x = (scaledWidth - viewSize.width).half)
            else
                Offset.create(y = (scaledHeight - viewSize.height).half)
        }

        else -> Offset.Zero
    }
}

internal fun Offset.transform(scaleFactor: Float, postScaleOffset: Offset): Offset = Offset(
    x = (x * scaleFactor) - postScaleOffset.x,
    y = (y * scaleFactor) - postScaleOffset.y
)

internal fun Offset.Companion.create(x: Float = 0f, y: Float = 0f): Offset = Offset(x = x, y = y)

internal val Float.half: Float get() = this / 2f
internal val Size.aspectRatio: Float get() = width / height

internal fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
internal fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)
