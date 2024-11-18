package com.scanner.app

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageAnalysis.Analyzer
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
import androidx.compose.ui.unit.dp
import com.scanner.app.ui.theme.AndroidDocumentScannerTheme
import com.scanner.library.DocumentScanner
import com.scanner.library.ScannedDocumentResult
import com.scanner.library.common.CameraView
import com.scanner.library.common.GraphicOverlay
import com.scanner.library.utils.returnUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            onResult = { result ->
                scannedBitmap = result.bitmap
                recognizedText = result.text
                imageSize = result.imageSize
                points.clear()
                points.addAll(result.points)
            }
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
                bitmap = scannedBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(0.9f)
                    .align(Alignment.TopCenter)
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
    context: Context,
    onResult: (ScannedDocumentResult) -> Unit,
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

    return Analyzer { imageProxy ->
        scope.launch(Dispatchers.IO) {
            try {
                onResult(
                    scanner.processImage(
                        context = context,
                        imageProxy = imageProxy,
                        isRecognizingText = true
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }.returnUnit()
    }
}


@Composable
@Preview(showBackground = true)
private fun Preview() {
    AndroidDocumentScannerTheme { App() }
}
