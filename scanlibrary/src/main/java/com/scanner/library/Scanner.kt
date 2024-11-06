package com.scanner.library

import android.app.AlertDialog
import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by jhansi on 28/03/15.
 */
class Scanner : AppCompatActivity(), IScanner, ComponentCallbacks2 {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scan_layout)
        init()
    }

    private fun init() {
        val fragment = PickImageFragment()
        val bundle = Bundle()
        bundle.putInt(ScanConstants.OPEN_INTENT_PREFERENCE, getPreferenceContent())
        fragment.setArguments(bundle)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.content, fragment)
        fragmentTransaction.commit()
    }

    protected fun getPreferenceContent(): Int {
        return intent.getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0)
    }

    override fun onBitmapSelect(uri: Uri?) {
        val fragment = ScanFragment()
        val bundle = Bundle()
        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, uri)
        fragment.setArguments(bundle)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.content, fragment)
        fragmentTransaction.addToBackStack(ScanFragment::class.java.toString())
        fragmentTransaction.commit()
    }

    override fun onScanFinish(uri: Uri?) {
        val fragment = ResultFragment()
        val bundle = Bundle()
        bundle.putParcelable(ScanConstants.SCANNED_RESULT, uri)
        fragment.setArguments(bundle)
        val fragmentManager = getSupportFragmentManager()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.content, fragment)
        fragmentTransaction.addToBackStack(ResultFragment::class.java.toString())
        fragmentTransaction.commit()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {}
            TRIM_MEMORY_RUNNING_MODERATE, TRIM_MEMORY_RUNNING_LOW, TRIM_MEMORY_RUNNING_CRITICAL -> {}
            TRIM_MEMORY_BACKGROUND, TRIM_MEMORY_MODERATE, TRIM_MEMORY_COMPLETE ->                 /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
                AlertDialog.Builder(this)
                    .setTitle(R.string.low_memory)
                    .setMessage(R.string.low_memory_message)
                    .create()
                    .show()

            else -> {}
        }
    }

    external fun getScannedBitmap(
        bitmap: Bitmap?,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        x4: Float,
        y4: Float
    ): Bitmap?

    external fun getGrayBitmap(bitmap: Bitmap?): Bitmap?

    external fun getMagicColorBitmap(bitmap: Bitmap?): Bitmap?

    external fun getBWBitmap(bitmap: Bitmap?): Bitmap?

    external fun getPoints(bitmap: Bitmap?): FloatArray?

    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("Scanner")
        }
    }
}
