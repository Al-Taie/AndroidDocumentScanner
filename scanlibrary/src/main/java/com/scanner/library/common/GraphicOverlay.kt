package com.scanner.library.common;

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize


@Composable
fun GraphicOverlay(
    modifier: Modifier = Modifier,
    imageSize: Size,
    points: List<Offset>,
    color: Color = Color.Green.copy(alpha = 0.15f),
) {
    var scaleFactor by remember { mutableFloatStateOf(1.0f) }
    var postScaleOffset by remember { mutableStateOf(Offset.Zero) }
    var needUpdateTransformation by remember { mutableStateOf(true) }

    fun updateTransformationIfNeeded(layoutCoordinates: LayoutCoordinates) {
        if (needUpdateTransformation.not() || imageSize.width <= 0 || imageSize.height <= 0) return

        val viewSize = layoutCoordinates.size.toSize()
        if (viewSize.aspectRatio > imageSize.aspectRatio) {
            scaleFactor = viewSize.width / imageSize.width
            postScaleOffset = viewSize.calculatePostScaleOffset(imageSize).copy(x = 0f)
        } else {
            scaleFactor = viewSize.height / imageSize.height
            postScaleOffset = viewSize.calculatePostScaleOffset(imageSize).copy(y = 0f)
        }
        needUpdateTransformation = false
    }

    fun List<Offset>.transform(): List<Offset> = runCatching {
        points.map { point -> point.transform(scaleFactor, postScaleOffset) }
    }.getOrDefault(emptyList())

    Canvas(modifier = modifier.onGloballyPositioned(::updateTransformationIfNeeded)) {
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

private fun Offset.transform(scaleFactor: Float, postScaleOffset: Offset): Offset =
    Offset(x = (x * scaleFactor) - postScaleOffset.x, y = (y * scaleFactor) - postScaleOffset.y)

private val Float.half: Float get() = this / 2f
private val Size.aspectRatio: Float get() = width / height
private fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
private fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)

private fun Size.calculatePostScaleOffset(other: Size): Offset {
    val postScaleOffset = (height * other.aspectRatio - width).half
    return Offset(postScaleOffset, postScaleOffset)
}
