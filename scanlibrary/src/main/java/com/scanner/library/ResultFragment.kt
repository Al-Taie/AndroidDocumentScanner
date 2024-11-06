package com.scanner.library

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.io.IOException
import java.lang.Exception

/**
 * Created by jhansi on 29/03/15.
 */
class ResultFragment : Fragment() {
    private var view: View? = null
    private var scannedImageView: ImageView? = null
    private var doneButton: Button? = null
    private var original: Bitmap? = null
    private var originalButton: Button? = null
    private var MagicColorButton: Button? = null
    private var grayModeButton: Button? = null
    private var bwButton: Button? = null
    private var transformed: Bitmap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.result_layout, null)
        init()
        return view!!
    }

    private fun init() {
        scannedImageView = view!!.findViewById<View?>(R.id.scannedImage) as ImageView
        originalButton = view!!.findViewById<View?>(R.id.original) as Button
        originalButton!!.setOnClickListener(OriginalButtonClickListener())
        MagicColorButton = view!!.findViewById<View?>(R.id.magicColor) as Button
        MagicColorButton!!.setOnClickListener(MagicColorButtonClickListener())
        grayModeButton = view!!.findViewById<View?>(R.id.grayMode) as Button
        grayModeButton!!.setOnClickListener(GrayButtonClickListener())
        bwButton = view!!.findViewById<View?>(R.id.BWMode) as Button
        bwButton!!.setOnClickListener(BWButtonClickListener())
        val bitmap = getBitmap()
        setScannedImage(bitmap)
        doneButton = view!!.findViewById<View?>(R.id.doneButton) as Button
        doneButton!!.setOnClickListener(DoneButtonClickListener())
    }

    private fun getBitmap(): Bitmap? {
        val uri = getUri()
        try {
            original = Utils.getBitmap(getActivity(), uri)
            getActivity()!!.getContentResolver().delete(uri, null, null)
            return original
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getUri(): Uri {
        val uri: Uri = getArguments()!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)!!
        return uri
    }

    fun setScannedImage(scannedImage: Bitmap?) {
        scannedImageView!!.setImageBitmap(scannedImage)
    }

    private inner class DoneButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            showProgressDialog(getResources().getString(R.string.loading))
            AsyncTask.execute(object : Runnable {
                override fun run() {
                    try {
                        val data = Intent()
                        var bitmap = transformed
                        if (bitmap == null) {
                            bitmap = original
                        }
                        val uri = Utils.getUri(getActivity(), bitmap)
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri)
                        getActivity()!!.setResult(Activity.RESULT_OK, data)
                        original!!.recycle()
                        System.gc()
                        getActivity()!!.runOnUiThread(object : Runnable {
                            override fun run() {
                                dismissDialog()
                                getActivity()!!.finish()
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }

    private inner class BWButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            showProgressDialog(getResources().getString(R.string.applying_filter))
            AsyncTask.execute(object : Runnable {
                override fun run() {
                    try {
                        transformed = (getActivity() as Scanner).getBWBitmap(original)
                    } catch (e: OutOfMemoryError) {
                        getActivity()!!.runOnUiThread(object : Runnable {
                            override fun run() {
                                transformed = original
                                scannedImageView!!.setImageBitmap(original)
                                e.printStackTrace()
                                dismissDialog()
                                onClick(v)
                            }
                        })
                    }
                    getActivity()!!.runOnUiThread(object : Runnable {
                        override fun run() {
                            scannedImageView!!.setImageBitmap(transformed)
                            dismissDialog()
                        }
                    })
                }
            })
        }
    }

    private inner class MagicColorButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            showProgressDialog(getResources().getString(R.string.applying_filter))
            AsyncTask.execute(object : Runnable {
                override fun run() {
                    try {
                        transformed = (getActivity() as Scanner).getMagicColorBitmap(original)
                    } catch (e: OutOfMemoryError) {
                        getActivity()!!.runOnUiThread(object : Runnable {
                            override fun run() {
                                transformed = original
                                scannedImageView!!.setImageBitmap(original)
                                e.printStackTrace()
                                dismissDialog()
                                onClick(v)
                            }
                        })
                    }
                    getActivity()!!.runOnUiThread(object : Runnable {
                        override fun run() {
                            scannedImageView!!.setImageBitmap(transformed)
                            dismissDialog()
                        }
                    })
                }
            })
        }
    }

    private inner class OriginalButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            try {
                showProgressDialog(getResources().getString(R.string.applying_filter))
                transformed = original
                scannedImageView!!.setImageBitmap(original)
                dismissDialog()
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                dismissDialog()
            }
        }
    }

    private inner class GrayButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            showProgressDialog(getResources().getString(R.string.applying_filter))
            AsyncTask.execute(object : Runnable {
                override fun run() {
                    try {
                        transformed = (getActivity() as Scanner).getGrayBitmap(original)
                    } catch (e: OutOfMemoryError) {
                        getActivity()!!.runOnUiThread(object : Runnable {
                            override fun run() {
                                transformed = original
                                scannedImageView!!.setImageBitmap(original)
                                e.printStackTrace()
                                dismissDialog()
                                onClick(v)
                            }
                        })
                    }
                    getActivity()!!.runOnUiThread(object : Runnable {
                        override fun run() {
                            scannedImageView!!.setImageBitmap(transformed)
                            dismissDialog()
                        }
                    })
                }
            })
        }
    }

    @Synchronized
    protected fun showProgressDialog(message: String?) {
        if (progressDialogFragment != null && progressDialogFragment!!.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment!!.dismissAllowingStateLoss()
        }
        progressDialogFragment = null
        progressDialogFragment = ProgressDialogFragment(message)
        val fm = getFragmentManager()
        progressDialogFragment.show(
            fm,
            ProgressDialogFragment::class.java.toString()
        )
    }

    @Synchronized
    protected fun dismissDialog() {
        progressDialogFragment!!.dismissAllowingStateLoss()
    }

    companion object {
        private var progressDialogFragment: ProgressDialogFragment? = null
    }
}
