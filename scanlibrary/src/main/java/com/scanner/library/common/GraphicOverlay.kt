package com.scanner.library.common

import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ScaleType
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize


@Composable
fun PreviewView.GraphicOverlay(
    modifier: Modifier = Modifier,
    imageSize: Size,
    points: List<Offset>,
    color: Color = Color.Green.copy(alpha = 0.15f),
) {
    var scaleFactor by remember { mutableFloatStateOf(1.0f) }
    var postScaleOffset by remember { mutableStateOf(Offset.Zero) }
    var needUpdateTransformation by remember { mutableStateOf(true) }

    /**
     * Updates the transformation based on the layout and image size.
     */
    fun updateTransformation(layoutCoordinates: LayoutCoordinates) {
        if (!needUpdateTransformation || imageSize.width <= 0 || imageSize.height <= 0) return

        val viewSize = layoutCoordinates.size.toSize()
        scaleFactor = scaleType.calculateScaleFactor(viewSize = viewSize, imageSize = imageSize)
        postScaleOffset = scaleType.calculatePostScaleOffset(
            viewSize = viewSize,
            imageSize = imageSize,
            scaleFactor = scaleFactor
        )

        needUpdateTransformation = false
    }

    /**
     * Transforms the list of points using the calculated scale factor and offset.
     */
    fun List<Offset>.transform(): List<Offset> = runCatching {
        points.map { point -> point.transform(scaleFactor, postScaleOffset) }
    }.getOrDefault(emptyList())

    Canvas(modifier = modifier.onGloballyPositioned(::updateTransformation)) {
        val transformedPoints = points.transform()
        if (transformedPoints.isEmpty()) return@Canvas

        val path = Path().apply {
            moveTo(offset = transformedPoints.first())
            transformedPoints.forEach(::lineTo)
            close()
        }
        drawPath(path = path, color = color)
    }
}

private fun ScaleType.calculateScaleFactor(
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

private fun ScaleType.calculatePostScaleOffset(
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

private fun Offset.transform(scaleFactor: Float, postScaleOffset: Offset): Offset = Offset(
    x = (x * scaleFactor) - postScaleOffset.x,
    y = (y * scaleFactor) - postScaleOffset.y
)

private fun Offset.Companion.create(x: Float = 0f, y: Float = 0f): Offset = Offset(x = x, y = y)

private val Float.half: Float get() = this / 2f
private val Size.aspectRatio: Float get() = width / height
private fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
private fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)
