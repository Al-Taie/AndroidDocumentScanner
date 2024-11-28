package com.scanner.library

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import com.googlecode.tesseract.android.TessBaseAPI
import com.scanner.library.utils.init
import com.scanner.library.utils.isValid
import com.scanner.library.utils.recognize
import com.scanner.library.utils.rotate
import com.scanner.library.utils.size
import com.scanner.library.utils.toOffsetPoints
import com.scanner.library.utils.toRotatedBitmap


class DocumentScanner {
    private val scanner by lazy { NativeScanner() }
    private var distance: Float = 0f

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

    fun getBestPoints(bitmap: Bitmap): List<Offset> =
        getPoints(getGrayBitmap(bitmap)).takeIf(List<Offset>::isValid) ?: emptyList()

    fun getBwBitmap(bitmap: Bitmap?): Bitmap? = scanner.getBwBitmap(bitmap)

    fun getGrayBitmap(bitmap: Bitmap?): Bitmap? = scanner.getGrayBitmap(bitmap)

    fun getMagicColorBitmap(bitmap: Bitmap?): Bitmap? = scanner.getMagicColorBitmap(bitmap)

    fun getPoints(bitmap: Bitmap?): List<Offset> {
        val points = scanner.getPoints(bitmap)
        distance = points?.lastOrNull() ?: 0f
        return points.toOffsetPoints()
    }

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

    suspend fun recognizeText(
        context: Context,
        bitmap: Bitmap?,
        rotationDegrees: Int = 0
    ): String? = runCatching {
        requireNotNull(bitmap)
        val tessBaseAPI = TessBaseAPI()
        tessBaseAPI.init(context)
        val rotatedBitmap = if (bitmap.height > bitmap.width)
            bitmap.rotate(degrees = -rotationDegrees.toFloat())
        else
            bitmap
        return tessBaseAPI.recognize(rotatedBitmap)
    }.onFailure { Log.e("DEBUGGING", "recognizeText: $it") }
        .getOrNull()

    suspend fun processImage(
        context: Context,
        imageProxy: ImageProxy,
        isRecognizingText: Boolean,
    ): ScannedDocumentResult = processImage(
        context = context,
        image = imageProxy,
        isRecognizingText = isRecognizingText
    )

    suspend fun processImage(
        context: Context,
        bitmap: Bitmap,
        isRecognizingText: Boolean,
    ): ScannedDocumentResult = processImage(
        context = context,
        image = bitmap,
        isRecognizingText = isRecognizingText
    )

    private suspend fun processImage(
        context: Context,
        image: Any,
        isRecognizingText: Boolean,
    ): ScannedDocumentResult {
        var rotationDegrees = 0
        val bitmap = when (image) {
            is Bitmap -> image
            is ImageProxy -> with(image) {
                rotationDegrees = imageInfo.rotationDegrees
                toRotatedBitmap()
            }

            else -> return ScannedDocumentResult.Empty
        }
        val bestPoints = getBestPoints(bitmap = bitmap)

        if (bestPoints.isEmpty()) return ScannedDocumentResult.Empty

        val scannedImage = getScannedBitmap(
            bitmap = bitmap,
            points = bestPoints
        )

        val recognizedText = if (isRecognizingText)
            recognizeText(
                context = context,
                bitmap = scannedImage,
                rotationDegrees = rotationDegrees
            )
        else null

        return ScannedDocumentResult(
            bitmap = scannedImage,
            points = bestPoints,
            text = recognizedText,
            imageSize = bitmap.size,
            state = when (distance) {
                in 0f..<10.5f -> DocumentState.GoFurther(distance = distance)
                in 10.5f..12.5f -> DocumentState.Correct(distance = distance)
                else -> DocumentState.ComeCloser(distance = distance)
            }
        )
    }

}
