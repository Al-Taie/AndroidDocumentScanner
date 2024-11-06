package com.scanner.library

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Created by jhansi on 05/04/15.
 */
object Utils {
    fun getUri(context: Context, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null)
        return Uri.parse(path)
    }

    @Throws(IOException::class)
    fun getBitmap(context: Context, uri: Uri?): Bitmap? {
        val bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri)
        return bitmap
    }
}
