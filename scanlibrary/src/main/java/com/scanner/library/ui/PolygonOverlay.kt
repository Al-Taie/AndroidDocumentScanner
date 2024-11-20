package com.scanner.library.ui


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.scanner.library.utils.getOrZero
import com.scanner.library.utils.rememberDerivedStateOf
import com.scanner.library.utils.rememberMutableStateOf
import kotlin.math.roundToInt


@Composable
fun PolygonOverlay(
    points: List<Offset>,
    modifier: Modifier = Modifier,
    lineWidth: Dp = 4.dp,
    pointSize: Dp = 10.dp,
    pointColor: Color = MaterialTheme.colorScheme.primary,
    lineColor: Color = MaterialTheme.colorScheme.secondary,
    onPointsChanged: (List<Offset>) -> Unit = {}
) {
    var topLeft by rememberMutableStateOf(key = points, value = points.getOrZero(0))
    var topRight by rememberMutableStateOf(key = points, value = points.getOrZero(1))
    var bottomLeft by rememberMutableStateOf(key = points, value = points.getOrZero(2))
    var bottomRight by rememberMutableStateOf(key = points, value = points.getOrZero(3))
    val topMid by rememberDerivedStateOf {
        Offset(
            x = (topLeft.x + topRight.x) / 2f,
            y = (topLeft.y + topRight.y) / 2f
        )
    }
    val bottomMid by rememberDerivedStateOf {
        Offset(
            x = (bottomLeft.x + bottomRight.x) / 2f,
            y = (bottomLeft.y + bottomRight.y) / 2f
        )
    }
    val leftMid by rememberDerivedStateOf {
        Offset(
            x = (topLeft.x + bottomLeft.x) / 2f,
            y = (topLeft.y + bottomLeft.y) / 2f
        )
    }
    val rightMid by rememberDerivedStateOf {
        Offset(
            x = (topRight.x + bottomRight.x) / 2f,
            y = (topRight.y + bottomRight.y) / 2f
        )
    }

    fun emitPoints() = onPointsChanged(
        listOf(
            topLeft,
            topRight,
            bottomLeft,
            bottomRight
        )
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply{
                moveTo(x = topLeft.x, y = topLeft.y)
                lineTo(x = topRight.x, y = topRight.y)
                lineTo(x = bottomRight.x, y = bottomRight.y)
                lineTo(x = bottomLeft.x, y = bottomLeft.y)
                close()
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = lineWidth.toPx())
            )
        }

        Circle(
            color = pointColor,
            offset = topLeft,
            size = pointSize
        ) { delta ->
            topLeft += delta
            emitPoints()
        }
        Circle(
            color = pointColor,
            offset = topRight,
            size = pointSize
        ) { delta ->
            topRight += delta
            emitPoints()
        }
        Circle(
            color = pointColor,
            offset = bottomLeft,
            size = pointSize
        ) { delta ->
            bottomLeft += delta
            emitPoints()
        }
        Circle(
            color = pointColor,
            offset = bottomRight,
            size = pointSize
        ) { delta ->
            bottomRight += delta
            emitPoints()
        }

        Circle(
            color = pointColor,
            offset = topMid,
            size = pointSize
        ) { delta ->
            topLeft = topLeft.copy(y = topLeft.y + delta.y)
            topRight = topRight.copy(y = topRight.y + delta.y)
        }
        Circle(
            color = pointColor,
            offset = bottomMid,
            size = pointSize
        ) { delta ->
            bottomLeft = bottomLeft.copy(y = bottomLeft.y + delta.y)
            bottomRight = bottomRight.copy(y = bottomRight.y + delta.y)
        }
        Circle(
            color = pointColor,
            offset = leftMid,
            size = pointSize
        ) { delta ->
            topLeft = topLeft.copy(x = topLeft.x + delta.x)
            bottomLeft = bottomLeft.copy(x = bottomLeft.x + delta.x)
            emitPoints()
        }
        Circle(
            color = pointColor,
            offset = rightMid,
            size = pointSize
        ) { delta ->
            topRight = topRight.copy(x = topRight.x + delta.x)
            bottomRight = bottomRight.copy(x = bottomRight.x + delta.x)
        }
    }
}


@Composable
private fun Circle(
    modifier: Modifier = Modifier,
    color: Color,
    size: Dp = 10.dp,
    offset: Offset,
    onOffsetChanged: (Offset) -> Unit
) {
    Canvas(
        modifier = modifier
            .offset {
                IntOffset(
                    x = offset.x.roundToInt(),
                    y = offset.y.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onOffsetChanged(dragAmount)
                }
            }
    ) { drawCircle(color = color, radius = size.toPx()) }
}


@Preview
@Composable
private fun PolygonOverlayPreview() {
    PolygonOverlay(
        points = listOf(
            Offset(x = 50f, y = 50f),
            Offset(x = 450f, y = 50f),
            Offset(x = 50f, y = 450f),
            Offset(x = 450f, y = 450f)
        )
    )
}
