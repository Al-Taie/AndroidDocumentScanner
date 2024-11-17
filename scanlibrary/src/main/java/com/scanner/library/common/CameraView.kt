package com.scanner.library.common


import android.content.res.Configuration
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executors


@Composable
fun CameraView(
    analyzer: Analyzer,
    modifier: Modifier = Modifier,
    onPreviewViewUpdate: (PreviewView) -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            val cameraProvider = cameraProviderFuture.get()

            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FIT_CENTER
            }.also { previewView ->
                cameraProviderFuture.addListener(
                    /* listener = */
                    {
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        val useCaseGroup = getCameraConfigurations(
                            analyzer = analyzer,
                            previewView = previewView
                        )

                        runCatching {
                            cameraProvider.unbindAll()
                        }

                        runCatching {
                            cameraProvider.bindToLifecycle(
                                /* lifecycleOwner = */ lifecycleOwner,
                                /* cameraSelector = */ cameraSelector,
                                /* useCaseGroup   = */ useCaseGroup
                            )
                        }
                    },
                    /* executor = */ executor,
                )
            }.also(onPreviewViewUpdate)
        },
        update = onPreviewViewUpdate
    )
}


private fun getCameraConfigurations(
    analyzer: Analyzer,
    previewView: PreviewView
): UseCaseGroup {
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val rotation = previewView.display.rotation
    val screenAspectRatio = if (rotation == Configuration.ORIENTATION_PORTRAIT)
        AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
    else
        AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY

    val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(screenAspectRatio)
        .build()

    val imageAnalysis = ImageAnalysis.Builder()
        .setResolutionSelector(resolutionSelector)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply { setAnalyzer(cameraExecutor, analyzer) }
        .apply { setAnalyzer(Dispatchers.IO.asExecutor(), analyzer) }

    val preview = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .build()
        .apply { surfaceProvider = previewView.surfaceProvider }

    return UseCaseGroup.Builder()
        .addUseCase(preview)
        .addUseCase(imageAnalysis)
        .apply { previewView.viewPort?.let(this::setViewPort) }
        .build()
}
