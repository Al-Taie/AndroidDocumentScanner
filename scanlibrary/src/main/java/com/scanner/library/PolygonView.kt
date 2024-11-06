package com.scanner.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.ArrayList
import java.util.HashMap
import kotlin.math.abs

/**
 * Created by jhansi on 28/03/15.
 */
class PolygonView : FrameLayout {
    protected var context: Context?
    private var paint: Paint? = null
    private var pointer1: ImageView? = null
    private var pointer2: ImageView? = null
    private var pointer3: ImageView? = null
    private var pointer4: ImageView? = null
    private var midPointer13: ImageView? = null
    private var midPointer12: ImageView? = null
    private var midPointer34: ImageView? = null
    private var midPointer24: ImageView? = null
    private var polygonView: PolygonView? = null

    constructor(context: Context) : super(context) {
        this.context = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        init()
    }

    private fun init() {
        polygonView = this
        pointer1 = getImageView(0, 0)
        pointer2 = getImageView(getWidth(), 0)
        pointer3 = getImageView(0, getHeight())
        pointer4 = getImageView(getWidth(), getHeight())
        midPointer13 = getImageView(0, getHeight() / 2)
        midPointer13!!.setOnTouchListener(PolygonView.MidPointTouchListenerImpl(pointer1, pointer3))

        midPointer12 = getImageView(0, getWidth() / 2)
        midPointer12!!.setOnTouchListener(PolygonView.MidPointTouchListenerImpl(pointer1, pointer2))

        midPointer34 = getImageView(0, getHeight() / 2)
        midPointer34!!.setOnTouchListener(PolygonView.MidPointTouchListenerImpl(pointer3, pointer4))

        midPointer24 = getImageView(0, getHeight() / 2)
        midPointer24!!.setOnTouchListener(PolygonView.MidPointTouchListenerImpl(pointer2, pointer4))

        addView(pointer1)
        addView(pointer2)
        addView(midPointer13)
        addView(midPointer12)
        addView(midPointer34)
        addView(midPointer24)
        addView(pointer3)
        addView(pointer4)
        initPaint()
    }

    override fun attachViewToParent(child: View?, index: Int, params: LayoutParams?) {
        super.attachViewToParent(child, index, params)
    }

    private fun initPaint() {
        paint = Paint()
        paint!!.setColor(getResources().getColor(R.color.blue))
        paint!!.setStrokeWidth(2f)
        paint!!.setAntiAlias(true)
    }

    fun getPoints(): MutableMap<Int?, PointF?> {
        val points: MutableList<PointF> = ArrayList<PointF>()
        points.add(PointF(pointer1!!.getX(), pointer1!!.getY()))
        points.add(PointF(pointer2!!.getX(), pointer2!!.getY()))
        points.add(PointF(pointer3!!.getX(), pointer3!!.getY()))
        points.add(PointF(pointer4!!.getX(), pointer4!!.getY()))

        return getOrderedPoints(points)
    }

    fun getOrderedPoints(points: MutableList<PointF>): MutableMap<Int?, PointF?> {
        val centerPoint = PointF()
        val size = points.size
        for (pointF in points) {
            centerPoint.x += pointF.x / size
            centerPoint.y += pointF.y / size
        }
        val orderedPoints: MutableMap<Int?, PointF?> = HashMap<Int?, PointF?>()
        for (pointF in points) {
            var index = -1
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3
            }
            orderedPoints.put(index, pointF)
        }
        return orderedPoints
    }

    fun setPoints(pointFMap: MutableMap<Int?, PointF?>) {
        if (pointFMap.size == 4) {
            setPointsCoordinates(pointFMap)
        }
    }

    private fun setPointsCoordinates(pointFMap: MutableMap<Int?, PointF?>) {
        pointer1!!.setX(pointFMap.get(0)!!.x)
        pointer1!!.setY(pointFMap.get(0)!!.y)

        pointer2!!.setX(pointFMap.get(1)!!.x)
        pointer2!!.setY(pointFMap.get(1)!!.y)

        pointer3!!.setX(pointFMap.get(2)!!.x)
        pointer3!!.setY(pointFMap.get(2)!!.y)

        pointer4!!.setX(pointFMap.get(3)!!.x)
        pointer4!!.setY(pointFMap.get(3)!!.y)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(
            pointer1!!.getX() + (pointer1!!.getWidth() / 2),
            pointer1!!.getY() + (pointer1!!.getHeight() / 2),
            pointer3!!.getX() + (pointer3!!.getWidth() / 2),
            pointer3!!.getY() + (pointer3!!.getHeight() / 2),
            paint!!
        )
        canvas.drawLine(
            pointer1!!.getX() + (pointer1!!.getWidth() / 2),
            pointer1!!.getY() + (pointer1!!.getHeight() / 2),
            pointer2!!.getX() + (pointer2!!.getWidth() / 2),
            pointer2!!.getY() + (pointer2!!.getHeight() / 2),
            paint!!
        )
        canvas.drawLine(
            pointer2!!.getX() + (pointer2!!.getWidth() / 2),
            pointer2!!.getY() + (pointer2!!.getHeight() / 2),
            pointer4!!.getX() + (pointer4!!.getWidth() / 2),
            pointer4!!.getY() + (pointer4!!.getHeight() / 2),
            paint!!
        )
        canvas.drawLine(
            pointer3!!.getX() + (pointer3!!.getWidth() / 2),
            pointer3!!.getY() + (pointer3!!.getHeight() / 2),
            pointer4!!.getX() + (pointer4!!.getWidth() / 2),
            pointer4!!.getY() + (pointer4!!.getHeight() / 2),
            paint!!
        )
        midPointer13!!.setX(pointer3!!.getX() - ((pointer3!!.getX() - pointer1!!.getX()) / 2))
        midPointer13!!.setY(pointer3!!.getY() - ((pointer3!!.getY() - pointer1!!.getY()) / 2))
        midPointer24!!.setX(pointer4!!.getX() - ((pointer4!!.getX() - pointer2!!.getX()) / 2))
        midPointer24!!.setY(pointer4!!.getY() - ((pointer4!!.getY() - pointer2!!.getY()) / 2))
        midPointer34!!.setX(pointer4!!.getX() - ((pointer4!!.getX() - pointer3!!.getX()) / 2))
        midPointer34!!.setY(pointer4!!.getY() - ((pointer4!!.getY() - pointer3!!.getY()) / 2))
        midPointer12!!.setX(pointer2!!.getX() - ((pointer2!!.getX() - pointer1!!.getX()) / 2))
        midPointer12!!.setY(pointer2!!.getY() - ((pointer2!!.getY() - pointer1!!.getY()) / 2))
    }

    private fun getImageView(x: Int, y: Int): ImageView {
        val imageView = ImageView(context)
        val layoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        imageView.setLayoutParams(layoutParams)
        imageView.setImageResource(R.drawable.circle)
        imageView.setX(x.toFloat())
        imageView.setY(y.toFloat())
        imageView.setOnTouchListener(PolygonView.TouchListenerImpl())
        return imageView
    }

    private inner class MidPointTouchListenerImpl(
        mainPointer1: ImageView,
        mainPointer2: ImageView
    ) : OnTouchListener {
        var DownPT: PointF = PointF() // Record Mouse Position When Pressed Down
        var StartPT: PointF = PointF() // Record Start Position of 'img'

        private val mainPointer1: ImageView
        private val mainPointer2: ImageView

        init {
            this.mainPointer1 = mainPointer1
            this.mainPointer2 = mainPointer2
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.getAction()
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.getX() - DownPT.x, event.getY() - DownPT.y)

                    if (abs((mainPointer1.getX() - mainPointer2.getX()).toDouble()) > abs((mainPointer1.getY() - mainPointer2.getY()).toDouble())) {
                        if (((mainPointer2.getY() + mv.y + v.getHeight() < polygonView!!.getHeight()) && (mainPointer2.getY() + mv.y > 0))) {
                            v.setX((StartPT.y + mv.y).toInt().toFloat())
                            StartPT = PointF(v.getX(), v.getY())
                            mainPointer2.setY((mainPointer2.getY() + mv.y).toInt().toFloat())
                        }
                        if (((mainPointer1.getY() + mv.y + v.getHeight() < polygonView!!.getHeight()) && (mainPointer1.getY() + mv.y > 0))) {
                            v.setX((StartPT.y + mv.y).toInt().toFloat())
                            StartPT = PointF(v.getX(), v.getY())
                            mainPointer1.setY((mainPointer1.getY() + mv.y).toInt().toFloat())
                        }
                    } else {
                        if ((mainPointer2.getX() + mv.x + v.getWidth() < polygonView!!.getWidth()) && (mainPointer2.getX() + mv.x > 0)) {
                            v.setX((StartPT.x + mv.x).toInt().toFloat())
                            StartPT = PointF(v.getX(), v.getY())
                            mainPointer2.setX((mainPointer2.getX() + mv.x).toInt().toFloat())
                        }
                        if ((mainPointer1.getX() + mv.x + v.getWidth() < polygonView!!.getWidth()) && (mainPointer1.getX() + mv.x > 0)) {
                            v.setX((StartPT.x + mv.x).toInt().toFloat())
                            StartPT = PointF(v.getX(), v.getY())
                            mainPointer1.setX((mainPointer1.getX() + mv.x).toInt().toFloat())
                        }
                    }
                }

                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.getX()
                    DownPT.y = event.getY()
                    StartPT = PointF(v.getX(), v.getY())
                }

                MotionEvent.ACTION_UP -> {
                    var color = 0
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.blue)
                    } else {
                        color = getResources().getColor(R.color.orange)
                    }
                    paint!!.setColor(color)
                }

                else -> {}
            }
            polygonView!!.invalidate()
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    fun isValidShape(pointFMap: MutableMap<Int?, PointF?>): Boolean {
        return pointFMap.size == 4
    }

    private inner class TouchListenerImpl : OnTouchListener {
        var DownPT: PointF = PointF() // Record Mouse Position When Pressed Down
        var StartPT: PointF = PointF() // Record Start Position of 'img'

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.getAction()
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.getX() - DownPT.x, event.getY() - DownPT.y)
                    if (((StartPT.x + mv.x + v.getWidth()) < polygonView!!.getWidth() && (StartPT.y + mv.y + v.getHeight() < polygonView!!.getHeight())) && ((StartPT.x + mv.x) > 0 && StartPT.y + mv.y > 0)) {
                        v.setX((StartPT.x + mv.x).toInt().toFloat())
                        v.setY((StartPT.y + mv.y).toInt().toFloat())
                        StartPT = PointF(v.getX(), v.getY())
                    }
                }

                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.getX()
                    DownPT.y = event.getY()
                    StartPT = PointF(v.getX(), v.getY())
                }

                MotionEvent.ACTION_UP -> {
                    var color = 0
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.blue)
                    } else {
                        color = getResources().getColor(R.color.orange)
                    }
                    paint!!.setColor(color)
                }

                else -> {}
            }
            polygonView!!.invalidate()
            return true
        }
    }
}
