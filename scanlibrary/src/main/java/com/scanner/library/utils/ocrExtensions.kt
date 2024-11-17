package com.scanner.library.utils

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


suspend fun Context.extractAssets() = withContext(Dispatchers.IO) {
    val subDir = "tessdata"
    val modelsDir = File(filesDir, "tessdata")
    if (modelsDir.exists()) return@withContext
    modelsDir.mkdirs()
    with(assets) {
        list(subDir)?.forEach { fileName ->
            open("$subDir/$fileName")
                .use { inputStream ->
                    val outputFile = modelsDir.resolve(fileName)
                    outputFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                }.returnUnit()
        }
    }
}

suspend fun TessBaseAPI.init(context: Context): Boolean {
    context.extractAssets()
    return init(
        /* datapath = */ context.filesDir.absolutePath,
        /* language = */ "eng",
        /* ocrEngineMode = */ TessBaseAPI.OEM_LSTM_ONLY,
    )
}

suspend fun TessBaseAPI.recognize(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
    setImage(bitmap)
    val result = utF8Text
    recycle()
    return@withContext result
}
