package com.notrika.roundedimage


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import top.defaults.drawabletoolbox.DrawableBuilder


class RoundedImageView : AppCompatImageView {
    private var radus: Float = 0.toFloat()

    private var maskBitmap: Bitmap? = null

    private var drawableBitmap: Bitmap? = null

    constructor(context: Context) : super(context) {
        setup(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setup(context, attrs, defStyle)
    }

    private fun setup(context: Context, attrs: AttributeSet?, defStyle: Int) {
        if (attrs != null) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0)
            radus = typedArray.getDimension(R.styleable.RoundedImageView_cornerRadus, 0f)
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val sizeChanged = width != oldw || height != oldh
        val isValid = width > 0 && height > 0
        if (isValid && sizeChanged) {
            var maskCanvas = Canvas()
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            maskCanvas.setBitmap(maskBitmap)

            var shape = DrawableBuilder()
                .rectangle()
                .bottomLeftRadius(radus.toInt()) // in pixels
                .bottomRightRadius(radus.toInt()) // in pixels
                .topLeftRadius(radus.toInt()) // in pixels
                .topRightRadius(radus.toInt()) // in pixel
                .solidColor(Color.WHITE)
                .solidColorPressed(Color.WHITE)
                .build()
            shape.setBounds(0, 0, width, height)
            shape.draw(maskCanvas)
            drawableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            Canvas().setBitmap(drawableBitmap)
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (!isInEditMode) {
            val saveCount = canvas.saveLayer(
                0.0f,
                0.0f,
                width.toFloat(),
                height.toFloat(),
                null,
                Canvas.ALL_SAVE_FLAG
            )
            try {
                val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
                val drawable = drawable
                val drawableCanvas = canvas
                if (drawable != null) {
                    val imageMatrix = imageMatrix
                    if (imageMatrix == null) {// && mPaddingTop == 0 && mPaddingLeft == 0) {
                        drawable.draw(drawableCanvas)
                    } else {
                        val drawableSaveCount = drawableCanvas.saveCount
                        drawableCanvas.save()
                        drawableCanvas.concat(imageMatrix)
                        drawable.draw(drawableCanvas)
                        drawableCanvas.restoreToCount(drawableSaveCount)
                    }

                    drawablePaint.isFilterBitmap = false
                    drawablePaint.xfermode =
                        PORTER_DUFF_XFERMODE
                    drawableCanvas.drawBitmap(maskBitmap!!, 0.0f, 0.0f, drawablePaint)

                }

                drawablePaint.xfermode = null
                canvas.drawBitmap(drawableBitmap!!, 0.0f, 0.0f, drawablePaint)

            } catch (e: Exception) {
                val log = "Exception occured while drawing $id"
                Log.e(TAG, log, e)
            } finally {
                canvas.restoreToCount(saveCount)
            }

        } else {
            super.onDraw(canvas)
        }
    }

    companion object {
        private val TAG = RoundedImageView::class.java.simpleName

        private val PORTER_DUFF_XFERMODE = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
}