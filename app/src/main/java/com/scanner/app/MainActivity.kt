package com.scanner.app

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scanner.app.ui.theme.AndroidDocumentScannerTheme
import com.scanner.library.DocumentScanner
import com.scanner.library.common.CameraView
import com.scanner.library.common.GraphicOverlay
import com.scanner.library.utils.returnUnit
import com.scanner.library.utils.size
import com.scanner.library.utils.toRotatedBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AndroidDocumentScannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val points = remember { mutableStateListOf<Offset>() }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    var scannedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    val analyzer = remember {
        createAnalyzer(
            scope = scope,
            context = context,
            points = points,
            updateImageSize = { imageSize = it },
            updateScannedBitmap = { scannedBitmap = it },
            updateRecognizedText = { recognizedText = it }
        )
    }

    CameraView(
        analyzer = analyzer,
        modifier = modifier.fillMaxSize(),
        onPreviewViewUpdate = { previewView = it }
    )

    previewView?.GraphicOverlay(
        points = points,
        imageSize = imageSize,
        modifier = modifier.fillMaxSize()
    )

    ScannedImageOverlay(scannedBitmap = scannedBitmap, recognizedText = recognizedText)
}

@Composable
fun ScannedImageOverlay(scannedBitmap: Bitmap?, recognizedText: String?) {
    if (scannedBitmap != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                scannedBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.aspectRatio(0.9f)
            )

            Text(
                text = recognizedText.orEmpty(),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            )
        }
    }
}

private fun createAnalyzer(
    scope: CoroutineScope,
    context: android.content.Context,
    points: MutableList<Offset>,
    updateImageSize: (Size) -> Unit,
    updateScannedBitmap: (Bitmap?) -> Unit,
    updateRecognizedText: (String?) -> Unit
): Analyzer {
    val scanner = DocumentScanner().apply {
        configureScanner(
            filterEnabled = true,
            applyCLAHE = true,
            scaleFactor = 2.0,
            contrastValue = 1.2,
            contrastLimitThreshold = 2.5
        )
    }

    return object : Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            scope.launch(Dispatchers.IO) {
                val bitmap = imageProxy.toRotatedBitmap()
                val bestPoints = scanner.getBestPoints(bitmap)

                if (bestPoints.isEmpty()) {
                    points.clear()
                    delay(250)
                    imageProxy.close()
                    return@launch
                }

                points.clear()
                points.addAll(bestPoints)

                val scannedImage = scanner.getScannedBitmap(bitmap, bestPoints)
                updateImageSize(bitmap.size)
                updateScannedBitmap(scannedImage)

                val text = scanner.recognizeText(
                    context = context,
                    bitmap = scannedImage,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees
                )
                updateRecognizedText(text)

                delay(500)
                imageProxy.close()
            }.returnUnit()
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    AndroidDocumentScannerTheme { App() }
}
