package com.otcengineering.vitesco.view.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.otcengineering.vitesco.R
import kotlin.math.max
import kotlin.math.min


class ArchProgressBar : LinearLayout {
    private var progress: Int = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        setWillNotDraw(false)
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.arch_progress_bar, this, true)
        this.postInvalidate()
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        this.invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val paint = Paint()
        val size = min(width, height).toFloat()
        paint.strokeWidth = size / 6
        paint.style = Paint.Style.STROKE
        val oval = RectF(0F, 0F, width.toFloat(), height.toFloat())
        oval.inset(size / 12, size / 12)

        paint.color = Color.LTGRAY
        val grayPath = Path()
        for (i in 0 until 6) {
            grayPath.arcTo(oval, 90 + (60 * i).toFloat(), 60F, true)
        }
        canvas.drawPath(grayPath, paint)

        when (progress) {
            0, 1 -> paint.color = Color.RED
            2 -> paint.color = Color.YELLOW
            else -> paint.color = Color.GREEN
        }
        val bluePath = Path()
        for (i in 0 until progress) {
            bluePath.arcTo(oval, 90 + (60 * i).toFloat(), 60F, true)
        }

        canvas.drawPath(bluePath, paint)

        paint.strokeWidth = 2F
        paint.color = 0xff000000.toInt()
        canvas.save()
        canvas.rotate(90F, size / 2, size / 2)
        for (i in 0 until 360 step 60) {
            canvas.rotate(60F,size/2,size/2);
            canvas.drawLine(size * 5 / 6,size / 2, size,size / 2, paint);
        }
        canvas.restore()

        val ovalOuter = RectF(0F, 0F, width.toFloat(), height.toFloat())
        ovalOuter.inset(1f, 1f)
        canvas.drawOval(ovalOuter, paint)

        val ovalInner = RectF(size / 6, size / 6, size * 5 / 6, size * 5 / 6)
        canvas.drawOval(ovalInner, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width: Int = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, max(800, heightMeasureSpec))
    }

    companion object {
        private const val START_ANGLE = 130f
        private const val ARCH_LENGTH = 50f
    }
}