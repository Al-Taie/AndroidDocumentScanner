package com.scanner.library

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size


data class ScannedDocumentResult(
    val text: String? = null,
    val bitmap: Bitmap? = null,
    val imageSize: Size = Size.Zero,
    val points: List<Offset> = emptyList(),
    val state: DocumentState = DocumentState.GoFurther(0f),
) {
    companion object {
        val Empty = ScannedDocumentResult()
    }
}
