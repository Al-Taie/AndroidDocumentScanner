package com.scanner.library

import android.os.Environment

/**
 * Created by jhansi on 15/03/15.
 */
object ScanConstants {
    const val PICKFILE_REQUEST_CODE: Int = 1
    const val START_CAMERA_REQUEST_CODE: Int = 2
    const val OPEN_INTENT_PREFERENCE: String = "selectContent"
    const val IMAGE_BASE_PATH_EXTRA: String = "ImageBasePath"
    const val OPEN_CAMERA: Int = 4
    const val OPEN_MEDIA: Int = 5
    const val SCANNED_RESULT: String = "scannedResult"
    val IMAGE_PATH: String = Environment
        .getExternalStorageDirectory().getPath() + "/scanSample"

    const val SELECTED_BITMAP: String = "selectedBitmap"
}
