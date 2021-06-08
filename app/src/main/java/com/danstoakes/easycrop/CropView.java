package com.danstoakes.easycrop;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class CropView extends View {

    private static final int BACKGROUND_COLOR = Color.WHITE;
    private static final int CROP_WIDTH = 5;

    private float mX, mY;
    private float mStartX, mStartY;
    private float mEndX, mEndY;

    private Path mPath;
    private Path mCropPath;
    private Paint mPaint;
    private Paint mLassoPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Point start;
    private Point end;

    private boolean cropping;
    private boolean invalid;
    private boolean lassoCrop;

    private int bitmapTop;
    private int bitmapLeft;
    private int mCropType;

    public ArrayList<Draw> paths = new ArrayList<>();

    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mLassoPaint = new Paint();
        mLassoPaint.setColor(Color.WHITE);
        mLassoPaint.setAlpha(80);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(BACKGROUND_COLOR);
        mPaint.setStrokeWidth(CROP_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
    }

    public void initialise(DisplayMetrics metrics) {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        canvas.drawColor(0x00AAAAAA);

        if (mBitmap != null) {
            mCanvas = canvas;

            if (cropping) {
                bitmapTop = Math.abs(getHeight() - mBitmap.getHeight()) / 2;
                bitmapLeft = Math.abs(getWidth() - mBitmap.getWidth()) / 2;
            }
            canvas.drawBitmap(mBitmap, bitmapLeft, bitmapTop, mPaint);
        }

        if (mCropType == 1) {
            canvas.drawRect(
                    Math.min(mStartX, mEndX),
                    Math.min(mStartY, mEndY),
                    Math.max(mEndX, mStartX),
                    Math.max(mEndY, mStartY),
                    mPaint);
        } else {
            for (Draw draw : paths) {
                mPaint.setStrokeWidth(draw.getWidth());
                mPaint.setMaskFilter(null);
                mPaint.setColor(draw.getColour());
                mPaint.setColorFilter(null);

                if (lassoCrop) {
                    canvas.drawPath(draw.getPath(), mLassoPaint);
                } else {
                    canvas.drawPath(draw.getPath(), mPaint);
                }
            }
        }

        canvas.restore();
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        cropping = true;

        invalidate();
    }

    public void setCropType(int cropType) {
        mCropType = cropType;
    }

    public int getCropType () {
        return mCropType;
    }

    public Path getPath () {
        return mCropPath;
    }

    public void clearBitmap() {
        mBitmap = null;
        paths.clear();
        // cropping = false;

        mCanvas.drawColor(BACKGROUND_COLOR);

        invalidate();
    }

    public void clearCanvas() {
        paths.clear();
        mPath = null;
        mCropPath = null;

        mStartX = 0;
        mStartY = 0;
        mEndX = 0;
        mEndY = 0;

        cropping = true;

        mCanvas.drawColor(BACKGROUND_COLOR);

        invalidate();
    }

    public Bitmap cropBitmap () {
        return Bitmap.createBitmap(mBitmap, (int) Math.min(mStartX, mEndX) - bitmapLeft, (int) Math.min(mStartY, mEndY) - bitmapTop, (int) Math.abs(mEndX - mStartX), (int) Math.abs(mEndY - mStartY));
    }

    public void setLassoCrop () {
        lassoCrop = !lassoCrop;

        clearCanvas();
    }

    public boolean getLassoCrop ()
    {
        return lassoCrop;
    }

    private void touchStart (float x, float y) {
        if (y > bitmapTop && y < (bitmapTop + mBitmap.getHeight()) && x > bitmapLeft && x < (bitmapLeft + mBitmap.getWidth())) {
            mPath = new Path();
            mCropPath = new Path();

            Draw draw = new Draw(Color.WHITE, 5, mPath);
            paths.add(draw);

            mPath.reset();
            mCropPath.reset();
            mPath.moveTo(x, y);
            mCropPath.moveTo(x - bitmapLeft, y - bitmapTop);

            mX = x;
            mY = y;

            start = new Point((int) mX, (int) mY);
        } else {
            invalid = true;
        }
    }

    private void touchStartRectangle (float x, float y) {
        if (y > bitmapTop && y < (bitmapTop + mBitmap.getHeight()) && x > bitmapLeft && x < (bitmapLeft + mBitmap.getWidth())) {
            mCropPath = new Path();

            mStartX = x;
            mStartY = y;
            if (mEndX == 0 || mEndY == 0) {
                mEndX = mStartX;
                mEndY = mStartY;
            }
        } else {
            invalid = true;
        }
    }

    private void touchMove (float x, float y) {
        if (!invalid && cropping) {
            if (y > bitmapTop && y < (bitmapTop + mBitmap.getHeight()) && x > bitmapLeft && x < (bitmapLeft + mBitmap.getWidth())) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mCropPath.quadTo(mX - bitmapLeft, mY - bitmapTop, ((x + mX) / 2) - bitmapLeft, ((y + mY) / 2) - bitmapTop);

                mX = x;
                mY = y;
            } else {
                if (y < bitmapTop) {
                    mPath.quadTo(mX, mY, (x + mX) / 2, bitmapTop);

                    mX = x;
                    mY = bitmapTop;
                } else if (y > (bitmapTop + mBitmap.getHeight())) {
                    mPath.quadTo(mX, mY, (x + mX) / 2, (bitmapTop + mBitmap.getHeight()));

                    mX = x;
                    mY = (bitmapTop + mBitmap.getHeight());
                } else if (x < bitmapLeft) {
                    mPath.quadTo(mX, mY, bitmapLeft, (y + mY) / 2);

                    mX = bitmapLeft;
                    mY = y;
                } else if (x > (bitmapLeft + mBitmap.getWidth())) {
                    mPath.quadTo(mX, mY, (bitmapLeft + mBitmap.getWidth()), (y + mY) / 2);

                    mX = bitmapLeft + mBitmap.getWidth();
                    mY = y;
                }
            }
        }
    }

    private void touchMoveRectangle (float x, float y) {
        if (!invalid && cropping) {
            if (Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                if (y > bitmapTop && y < (bitmapTop + mBitmap.getHeight()) && x > bitmapLeft && x < (bitmapLeft + mBitmap.getWidth())) {
                    mEndX = x;
                    mEndY = y;
                } else {
                    if (y < bitmapTop) {
                        mEndX = x;
                        mEndY = bitmapTop;
                    } else if (y > (bitmapTop + mBitmap.getHeight())) {
                        mEndX = x;
                        mEndY = (bitmapTop + mBitmap.getHeight());
                    } else if (x < bitmapLeft) {
                        mEndX = bitmapLeft;
                        mEndY = y;
                    } else if (x > (bitmapLeft + mBitmap.getWidth())) {
                        mEndX = bitmapLeft + mBitmap.getWidth();
                        mEndY = y;
                    }
                }
            }
        }
    }

    private void touchUp () {
        if (!invalid && cropping) {
            mPath.lineTo(mX, mY);
            mCropPath.lineTo(mX - bitmapLeft, mY - bitmapTop);
            end = new Point((int) mX, (int) mY);
            cropping = false;

            Draw draw = new Draw(Color.WHITE, 5, mPath);
            paths.add(draw);

            mPath.moveTo(start.x, start.y);
            mCropPath.moveTo(start.x, start.y - bitmapTop);
            mPath.lineTo(end.x, end.y);
            mCropPath.lineTo(end.x, end.y - bitmapTop);
        }
        invalid = false;
    }

    public void handleMotion (MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (mCropType) {
            case 1: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        touchStartRectangle(x, y);
                        invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (Math.abs(mStartX - mEndX) < 5 || Math.abs(mStartY - mEndY) < 5) {
                            clearCanvas();
                        } else {
                            invalid = false;
                        }
                        invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        touchMoveRectangle(x, y);
                        invalidate();
                        break;
                    }
                }
                break;
            }
            case 2: {
                if (!cropping) {
                    Toast.makeText(getContext(), "Press back to select a new area", Toast.LENGTH_LONG).show();
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            touchStart(x, y);
                            invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            touchUp();
                            invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            touchMove(x, y);
                            invalidate();
                            break;
                        }
                    }
                }
                break;
            }
        }

    }
}