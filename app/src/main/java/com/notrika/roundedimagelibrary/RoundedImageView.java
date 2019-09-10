package com.notrika.roundedimagelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;

import top.defaults.drawabletoolbox.DrawableBuilder;


public class RoundedImageView extends AppCompatImageView {
    private static final String TAG = RoundedImageView.class.getSimpleName();
    private Drawable shape;
    private float radus;

    private static final PorterDuffXfermode PORTER_DUFF_XFERMODE = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    private Canvas maskCanvas;
    private Bitmap maskBitmap;
    private Paint maskPaint;

    private Canvas drawableCanvas;
    private Bitmap drawableBitmap;
    private Paint drawablePaint;

    public RoundedImageView(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs, defStyle);
    }

    private void setup(Context context, AttributeSet attrs, int defStyle) {
        if(attrs != null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);
            radus = typedArray.getDimension(R.styleable.RoundedImageView_cornerRadus,0f);
            typedArray.recycle();
        }

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shape = new DrawableBuilder()
                .rectangle()
                .bottomLeftRadius((int) radus) // in pixels
                .bottomRightRadius((int) radus) // in pixels
                .topLeftRadius((int) radus) // in pixels
                .topRightRadius((int) radus) // in pixel
                .solidColor(Color.WHITE)
                .solidColorPressed(Color.WHITE)
                .build();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createMaskCanvas(w, h, oldw, oldh);
    }

    private void createMaskCanvas(int width, int height, int oldw, int oldh) {
        boolean sizeChanged = width != oldw || height != oldh;
        boolean isValid = width > 0 && height > 0;
        if(isValid && (maskCanvas == null || sizeChanged)) {
            maskCanvas = new Canvas();
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            maskCanvas.setBitmap(maskBitmap);

            maskPaint.reset();
            if(shape != null) {
                shape.setBounds(0, 0, width, height);
                shape.draw(maskCanvas);
            }
            drawableCanvas = new Canvas();
            drawableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            drawableCanvas.setBitmap(drawableBitmap);
            drawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            int saveCount = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            try {
                    Drawable drawable = getDrawable();
                    if (drawable != null) {
                        Matrix imageMatrix = getImageMatrix();
                        if (imageMatrix == null){// && mPaddingTop == 0 && mPaddingLeft == 0) {
                            drawable.draw(drawableCanvas);
                        } else {
                            int drawableSaveCount = drawableCanvas.getSaveCount();
                            drawableCanvas.save();
                            drawableCanvas.concat(imageMatrix);
                            drawable.draw(drawableCanvas);
                            drawableCanvas.restoreToCount(drawableSaveCount);
                        }

                        drawablePaint.reset();
                        drawablePaint.setFilterBitmap(false);
                        drawablePaint.setXfermode(PORTER_DUFF_XFERMODE);
                        drawableCanvas.drawBitmap(maskBitmap, 0.0f, 0.0f, drawablePaint);
                    }

                    drawablePaint.setXfermode(null);
                    canvas.drawBitmap(drawableBitmap, 0.0f, 0.0f, drawablePaint);

            } catch (Exception e) {
                String log = "Exception occured while drawing " + getId();
                Log.e(TAG, log, e);
            } finally {
                canvas.restoreToCount(saveCount);
            }
        } else {
            super.onDraw(canvas);
        }
    }
}