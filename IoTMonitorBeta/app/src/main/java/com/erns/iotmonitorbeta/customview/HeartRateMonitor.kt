package com.erns.iotmonitorbeta.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View

class HeartRateMonitor(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val TAG = "HeartRateMonitor"
    private val heartRates = ArrayList<Float>()
    private val paint = Paint()
    private var paintGrid = Paint()
    private var WIDTH_COLUMN = 50f
    private var height_box = 100f
    private var BUFFER_SIZE: Int = 0
    private var WIDTH = 0f
    private var HEIGHT = 0f
    private var VERTICAL_SCALE = 1f

    init {
        paint.color = Color.GREEN
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        paintGrid.color = Color.WHITE
        paintGrid.strokeWidth = 1f
        paintGrid.style = Paint.Style.STROKE
        paintGrid.isAntiAlias = true

        val layout_width =
            attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_width")
                .replace("dip", "").toFloat()

        val layout_height =
            attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height")
                .replace("dip", "").toFloat()


        val density = resources.displayMetrics.density
        HEIGHT = density * layout_height
        WIDTH = density * layout_width
        BUFFER_SIZE = (WIDTH / WIDTH_COLUMN).toInt() - 1

        Log.d(TAG, "layout_width: $layout_width")

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawHeartSignal(canvas)
        drawGrid(canvas)
    }


    private fun drawHeartSignal(canvas: Canvas) {
        var xloc = 0f
        val yloc = HEIGHT
        val path = Path()
        path.moveTo(xloc, yloc)

        heartRates.forEach { value ->
            xloc += WIDTH_COLUMN
            path.lineTo(xloc, yloc - value)
        }

        canvas.drawPath(path, paint)
    }

    private fun drawGrid(canvas: Canvas) {
//        var px = 0f
        var py = HEIGHT - height_box
        while (py > 0) {
            canvas.drawLine(0f, py, WIDTH, py, paintGrid)
            py -= height_box
            Log.d(TAG, "py: $py")
        }
    }

    fun setVerticalRange(value: Float) {
        VERTICAL_SCALE = HEIGHT / value
    }

    fun setValue(value: Float) {
        if (heartRates.size > BUFFER_SIZE) heartRates.removeAt(0)
        heartRates.add(value * VERTICAL_SCALE)
        invalidate()
    }
}