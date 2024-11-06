package com.scanner.library

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.io.IOException
import java.lang.ClassCastException
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by jhansi on 29/03/15.
 */
class ScanFragment : Fragment() {
    private var scanButton: Button? = null
    private var sourceImageView: ImageView? = null
    private var sourceFrame: FrameLayout? = null
    private var polygonView: PolygonView? = null
    private var view: View? = null
    private var progressDialogFragment: ProgressDialogFragment? = null
    private var scanner: IScanner? = null
    private var original: Bitmap? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity !is IScanner) {
            throw ClassCastException("Activity must implement IScanner")
        }
        this.scanner = activity as IScanner
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.scan_fragment_layout, null)
        init()
        return view!!
    }

    private fun init() {
        sourceImageView = view!!.findViewById<View?>(R.id.sourceImageView) as ImageView
        scanButton = view!!.findViewById<View?>(R.id.scanButton) as Button
        scanButton!!.setOnClickListener(ScanFragment.ScanButtonClickListener())
        sourceFrame = view!!.findViewById<View?>(R.id.sourceFrame) as FrameLayout
        polygonView = view!!.findViewById<View?>(R.id.polygonView) as PolygonView
        sourceFrame!!.post(object : Runnable {
            override fun run() {
                original = getBitmap()
                if (original != null) {
                    setBitmap(original!!)
                }
            }
        })
    }

    private fun getBitmap(): Bitmap? {
        val uri = getUri()
        try {
            val bitmap = Utils.getBitmap(getActivity(), uri)
            getActivity()!!.getContentResolver().delete(uri, null, null)
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getUri(): Uri {
        val uri: Uri = getArguments()!!.getParcelable<Uri>(ScanConstants.SELECTED_BITMAP)!!
        return uri
    }

    private fun setBitmap(original: Bitmap) {
        val scaledBitmap =
            scaledBitmap(original, sourceFrame!!.getWidth(), sourceFrame!!.getHeight())
        sourceImageView!!.setImageBitmap(scaledBitmap)
        val tempBitmap = (sourceImageView!!.getDrawable() as BitmapDrawable).getBitmap()
        val pointFs = getEdgePoints(tempBitmap)
        polygonView!!.setPoints(pointFs)
        polygonView!!.setVisibility(View.VISIBLE)
        val padding = getResources().getDimension(R.dimen.scanPadding).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            tempBitmap.getWidth() + 2 * padding,
            tempBitmap.getHeight() + 2 * padding
        )
        layoutParams.gravity = Gravity.CENTER
        polygonView!!.setLayoutParams(layoutParams)
    }

    private fun getEdgePoints(tempBitmap: Bitmap): MutableMap<Int?, PointF?> {
        val pointFs = getContourEdgePoints(tempBitmap)
        val orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs)
        return orderedPoints
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap?): MutableList<PointF?> {
        val points = (getActivity() as Scanner).getPoints(tempBitmap)
        val x1 = points!![0]
        val x2 = points[1]
        val x3 = points[2]
        val x4 = points[3]

        val y1 = points[4]
        val y2 = points[5]
        val y3 = points[6]
        val y4 = points[7]

        val pointFs: MutableList<PointF?> = ArrayList<PointF?>()
        pointFs.add(PointF(x1, y1))
        pointFs.add(PointF(x2, y2))
        pointFs.add(PointF(x3, y3))
        pointFs.add(PointF(x4, y4))
        return pointFs
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): MutableMap<Int?, PointF?> {
        val outlinePoints: MutableMap<Int?, PointF?> = HashMap<Int?, PointF?>()
        outlinePoints.put(0, PointF(0f, 0f))
        outlinePoints.put(1, PointF(tempBitmap.getWidth().toFloat(), 0f))
        outlinePoints.put(2, PointF(0f, tempBitmap.getHeight().toFloat()))
        outlinePoints.put(
            3,
            PointF(tempBitmap.getWidth().toFloat(), tempBitmap.getHeight().toFloat())
        )
        return outlinePoints
    }

    private fun orderedValidEdgePoints(
        tempBitmap: Bitmap,
        pointFs: MutableList<PointF?>
    ): MutableMap<Int?, PointF?> {
        var orderedPoints = polygonView!!.getOrderedPoints(pointFs)
        if (!polygonView!!.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    private inner class ScanButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val points = polygonView!!.getPoints()
            if (isScanPointsValid(points)) {
                ScanAsyncTask(points).execute()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun showErrorDialog() {
        val fragment =
            SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true)
        val fm = requireActivity().getSupportFragmentManager()
        fragment.show(fm, SingleButtonDialogFragment::class.java.toString())
    }

    private fun isScanPointsValid(points: MutableMap<Int?, PointF?>): Boolean {
        return points.size == 4
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, bitmap.getWidth().toFloat(), bitmap.getHeight().toFloat()),
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true)
    }

    private fun getScannedBitmap(original: Bitmap, points: MutableMap<Int?, PointF?>): Bitmap? {
        val width = original.getWidth()
        val height = original.getHeight()
        val xRatio = original.getWidth().toFloat() / sourceImageView!!.getWidth()
        val yRatio = original.getHeight().toFloat() / sourceImageView!!.getHeight()

        val x1 = (points.get(0)!!.x) * xRatio
        val x2 = (points.get(1)!!.x) * xRatio
        val x3 = (points.get(2)!!.x) * xRatio
        val x4 = (points.get(3)!!.x) * xRatio
        val y1 = (points.get(0)!!.y) * yRatio
        val y2 = (points.get(1)!!.y) * yRatio
        val y3 = (points.get(2)!!.y) * yRatio
        val y4 = (points.get(3)!!.y) * yRatio
        Log.d(
            "",
            "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")"
        )
        val _bitmap =
            (getActivity() as Scanner).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4)
        return _bitmap
    }

    private inner class ScanAsyncTask(points: MutableMap<Int?, PointF?>) :
        AsyncTask<Void?, Void?, Bitmap?>() {
        private val points: MutableMap<Int?, PointF?>

        init {
            this.points = points
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(getString(R.string.scanning))
        }

        override fun doInBackground(vararg params: Void?): Bitmap {
            val bitmap = getScannedBitmap(original!!, points)
            val uri = Utils.getUri(getActivity(), bitmap)
            scanner!!.onScanFinish(uri)
            return bitmap!!
        }

        override fun onPostExecute(bitmap: Bitmap) {
            super.onPostExecute(bitmap)
            bitmap.recycle()
            dismissDialog()
        }
    }

    protected fun showProgressDialog(message: String?) {
        progressDialogFragment = ProgressDialogFragment(message)
        val fm = getFragmentManager()
        progressDialogFragment.show(fm, ProgressDialogFragment::class.java.toString())
    }

    protected fun dismissDialog() {
        progressDialogFragment!!.dismissAllowingStateLoss()
    }
}
