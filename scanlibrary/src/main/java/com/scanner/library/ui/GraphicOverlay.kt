package com.scanner.library.ui

import androidx.camera.view.PreviewView
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
import com.scanner.library.utils.calculatePostScaleOffset
import com.scanner.library.utils.calculateScaleFactor
import com.scanner.library.utils.moveTo
import com.scanner.library.utils.lineTo
import com.scanner.library.utils.transform


@Composable
fun PreviewView.GraphicOverlay(
    modifier: Modifier = Modifier,
    imageSize: Size,
    points: List<Offset>,
    color: Color = Color.Green.copy(alpha = 0.15f),
    onPointsUpdate: (List<Offset>) -> Unit = {},
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
    }.getOrDefault(emptyList()).also(onPointsUpdate)

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
