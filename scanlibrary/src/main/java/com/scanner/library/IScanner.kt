package com.scanner.library

import android.net.Uri

/**
 * Created by jhansi on 04/04/15.
 */
interface IScanner {
    fun onBitmapSelect(uri: Uri?)

    fun onScanFinish(uri: Uri?)
}
