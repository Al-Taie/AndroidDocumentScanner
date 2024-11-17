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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.scanner.app.ui.theme.AndroidDocumentScannerTheme
import com.scanner.library.DocumentScanner
import com.scanner.library.common.CameraView
import com.scanner.library.common.GraphicOverlay
import com.scanner.library.utils.returnUnit
import com.scanner.library.utils.size
import com.scanner.library.utils.toRotatedBitmap
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
    val scope = rememberCoroutineScope()
    val points = remember { mutableStateListOf<Offset>() }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    val context = LocalContext.current
    var recognizedText: String? by remember { mutableStateOf(null) }
    var scannedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    CameraView(
        modifier = modifier.fillMaxSize(),
        onPreviewViewUpdate = { previewView = it },
        analyzer = object : Analyzer {
            val scanner = DocumentScanner()

            init {
                scanner.configureScanner(
                    filterEnabled = false,
                    applyCLAHE = true,
                    scaleFactor = 2.0,
                    contrastValue = 1.2,
                    contrastLimitThreshold = 2.5
                )
            }

            override fun analyze(imageProxy: ImageProxy) = scope.launch(Dispatchers.IO) {
                val bitmap = imageProxy.toRotatedBitmap()
                val bestPoint: List<Offset> = scanner.getBestPoints(bitmap)
                if (bestPoint.isEmpty()) {
                    points.clear()
                    delay(250)
                    return@launch imageProxy.close()
                }
                points.clear()
                points.addAll(bestPoint)
                val scannedImage = scanner.getScannedBitmap(
                    bitmap,
                    bestPoint
                )
                imageSize = bitmap.size
                scannedBitmap = scannedImage
                recognizedText = scanner.recognizeText(
                    context = context,
                    bitmap = scannedImage,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees
                )
                delay(500)
                imageProxy.close()
            }.returnUnit()
        }
    )

    previewView?.GraphicOverlay(
        modifier = modifier.fillMaxSize(),
        imageSize = imageSize,
        points = points
    )

    if (scannedBitmap != null) Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            scannedBitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.aspectRatio(0.9f)
        )

        Text(
            text = recognizedText.orEmpty(), modifier = Modifier.background(
                color = Color.Black.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    AndroidDocumentScannerTheme { App() }
}
