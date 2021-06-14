package com.danstoakes.easycrop;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Class which handles any drawing and interaction with the canvas.
 */
public class CropView extends View
{
    public static final int CROP_CLASSIC = 1;
    public static final int CROP_FREEHAND = 2;
    public static final int CROP_LASSO = 3;

    private static final int BACKGROUND_COLOR = Color.WHITE;
    private static final int CROP_WIDTH = 5;

    private BitmapHandler bitmapHandler;
    private final FreehandCrop freehandCrop;
    private final RectangleCrop rectangleCrop;

    private Canvas mCanvas;
    private final Paint mPaint;
    private final Paint mLassoPaint;

    private boolean invalid;

    private int bitmapTop;
    private int bitmapLeft;
    private int mCropType;

    /**
     * Constructor which sets up the paint objects, as well as the crop helpers.
     * @param context - the context of the application.
     * @param attributeSet - any custom xml-defined attributes.
     */
    public CropView(Context context, AttributeSet attributeSet)
    {
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

        freehandCrop = new FreehandCrop();
        rectangleCrop = new RectangleCrop();
    }

    /**
     * Secondary constructor which handles the loading of the bitmap and canvas.
     * @param width - the width of the activity window.
     * @param height - the height of the activity window.
     */
    public void initialise(int width, int height, BitmapHandler bitmapHandler)
    {
        // set the BitmapHandler, scaled bitmap, and canvas.
        this.bitmapHandler = bitmapHandler;
        bitmapHandler.setScaledBitmap(Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565));
        mCanvas = new Canvas(bitmapHandler.getScaledBitmap());

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * Sets the bitmap to be cropped.
     * @param bitmap - the bitmap to be set on the canvas.
     */
    public void setBitmap(Bitmap bitmap)
    {
        // set the scaled bitmap and call for a canvas reload
        bitmapHandler.setScaledBitmap(bitmap);
        invalidate();
    }

    /**
     * Sets the crop type to be used.
     * @param cropType - the crop type to be used.
     */
    public void setCropType(int cropType)
    {
        // switches the crop type between freehand and lasso
        if (cropType == CROP_LASSO && mCropType == CROP_LASSO)
            cropType = CROP_FREEHAND;
        // set the crop type
        mCropType = cropType;
    }

    /**
     * Returns the current crop type.
     * @return int - the crop type.
     */
    public int getCropType ()
    {
        return mCropType;
    }

    /**
     * Returns whether the lasso crop is active or not.
     * @return boolean - whether the lasso crop is active.
     */
    public boolean hasLassoCropActive ()
    {
        return mCropType == CROP_LASSO;
    }

    /**
     * Returns whether a crop path has been drawn or not.
     * @return boolean - whether a crop path has been drawn.
     */
    public boolean hasPath ()
    {
        return freehandCrop.getCropPath() != null;
    }

    /**
     * Clears the loaded bitmap.
     */
    public void clearBitmap()
    {
        // remove the bitmap and drawn crop paths
        bitmapHandler.setScaledBitmap(null);
        freehandCrop.clearPathsList();
        // draw the background colour
        mCanvas.drawColor(BACKGROUND_COLOR);
        invalidate();
    }

    /**
     * Clears the canvas.
     */
    public void clearCanvas()
    {
        // clear the Freehand and Rectangle crop paths
        freehandCrop.clearPathsList();
        freehandCrop.emptyPaths();
        rectangleCrop.clearValues();
        // draw the background colour
        mCanvas.drawColor(BACKGROUND_COLOR);
        invalidate();
    }

    /**
     * Mirrors the bitmap in either the x or y axis depending on the image orientation.
     * @param width - the width of the image.
     * @param height - the height of the image.
     */
    public void flipBitmap (int width, int height)
    {
        // clear the bitmap and the canvas
        clearBitmap();
        clearCanvas();
        // flip the unscaled bitmap
        Bitmap flippedBitmap = BitmapHandler.flipBitmap(bitmapHandler.getUnscaledBitmap());
        bitmapHandler.setUnscaledBitmap(flippedBitmap);
        // scale the bitmap and set it on the canvas
        Bitmap scaledBitmap = BitmapHandler.getAsScaledBitmap(flippedBitmap, width, height);
        setBitmap(scaledBitmap);
    }

    /**
     * Rotates the bitmap 90 degrees clockwise.
     * @param width - the width of the image.
     * @param height - the height of the image.
     */
    public void rotateBitmap (int width, int height)
    {
        // clear the bitmap and the canvas
        clearBitmap();
        clearCanvas();
        // rotate the unscaled bitmap
        Bitmap rotatedBitmap = BitmapHandler.rotateBitmap(bitmapHandler.getUnscaledBitmap());
        bitmapHandler.setUnscaledBitmap(rotatedBitmap);
        // scale the bitmap and set it on the canvas
        Bitmap scaledBitmap = BitmapHandler.getAsScaledBitmap(
                rotatedBitmap, width, height);
        setBitmap(scaledBitmap);
    }

    /**
     * Crop the bitmap using the RectangleCrop class.
     * @return Bitmap - the cropped bitmap.
     */
    public Bitmap cropBitmap ()
    {
        try
        {
            // crop the bitmap using the left, top, width, and height coordinates
            return Bitmap.createBitmap(bitmapHandler.getScaledBitmap(),
                    (int) rectangleCrop.getLeft() - bitmapLeft, (int) rectangleCrop.getTop() - bitmapTop,
                    (int) rectangleCrop.getWidth(), (int) rectangleCrop.getHeight());
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * Crop the bitmap using the FreehandCrop class.
     * @return Bitmap - the cropped bitmap.
     */
    public Bitmap cropBitmapFreehand (int width, int height)
    {
        // get the path and return null if it hasn't been drawn
        Path path = freehandCrop.getCropPath();
        if (path == null)
            return null;
        // clone the existing unscaled bitmap and create a new canvas with it
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), bitmapHandler.getUnscaledBitmap().getConfig());
        Canvas canvas = new Canvas(bitmap);
        // get the Bitmap as a scaled representation
        Bitmap scaledBitmap = BitmapHandler.getAsScaledBitmap(bitmap, getWidth(), getHeight());
        // draw the crop path on the canvas
        Paint paint = new Paint();
        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);
        // crop the region of the bitmap
        Region region = new Region();
        Region clip = new Region(0, 0, bitmap.getWidth(), bitmap.getHeight());
        region.setPath(path, clip);
        Rect bounds = region.getBounds();
        // return the cropped bitmap
        return Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
    }

    /**
     * Handle any touches which are made on the CropView object.
     * @param event - the touch event.
     */
    public void handleMotion (MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();
        // perform a crop depending on the crop type.
        if (mCropType == CROP_CLASSIC)
        {
            classicCropMotion(event.getAction(), x, y);
        } else if (mCropType == CROP_FREEHAND || mCropType == CROP_LASSO)
        {
            freehandCropMotion(event.getAction(), x, y);
        }
    }

    /**
     * Handle any touches performed using the classic/rectangle crop.
     * @param action - the action being performed.
     * @param x - the x coordinate of the touch.
     * @param y - the y coordinate of the touch.
     */
    private void classicCropMotion (int action, float x, float y)
    {
        switch (action)
        {
            // determine which action is being performed and redraw the canvas
            case MotionEvent.ACTION_DOWN:
                // the user is pressing down on the canvas
                touchStartRectangle(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // the user is moving while pressing down on the canvas
                if (!rectangleCrop.hasMinimumStrokeLength())
                {
                    clearCanvas();
                } else {
                    invalid = false;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // the user has lifted up after pressing down on the canvas
                touchMoveRectangle(x, y);
                invalidate();
                break;
        }
    }

    /**
     * Handle any touches performed using the freehand/lasso crop.
     * @param action - the action being performed.
     * @param x - the x-ordinate of the touch.
     * @param y - the y-ordinate of the touch.
     */
    private void freehandCropMotion (int action, float x, float y)
    {
        // determine which action is being performed and redraw the canvas
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                // the user is pressing down on the canvas
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // the user is moving while pressing down on the canvas
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // the user has lifted up after pressing down on the canvas
                touchMove(x, y);
                invalidate();
                break;
        }
    }

    /**
     * Returns whether a coordinate is within the bounds of the scaled bitmap.
     * @param x - the x-ordinate of the touch.
     * @param y - the y-ordinate of the touch.
     * @return boolean - whether a coordinate is within the scaled bitmap.
     */
    private boolean isWithinBitmap (float x, float y)
    {
        int width = bitmapHandler.getScaledBitmap().getWidth();
        int height = bitmapHandler.getScaledBitmap().getHeight();
        // determine if the coordinates are within the bitmap
        return y > bitmapTop && y < (bitmapTop + height) && x > bitmapLeft && x < (bitmapLeft + width);
    }

    /**
     * Handle the user's first touch on the canvas.
     * @param x - the x-ordinate of the touch.
     * @param y - the y-ordinate of the touch.
     */
    private void touchStart (float x, float y)
    {
        if (isWithinBitmap(x, y))
        {
            // clear the canvas of existing paths and generate a new path
            clearCanvas();
            freehandCrop.createPaths();
            freehandCrop.addDrawPath(new DrawPath(Color.WHITE, 5, freehandCrop.getPath()));
            // start drawing the crop path at the coordinate
            freehandCrop.resetPaths();
            freehandCrop.pathMoveTo(x, y);
            freehandCrop.cropPathMoveTo(x - bitmapLeft, y - bitmapTop);
            // set the coordinates to be used in touchMove/touchUp
            freehandCrop.setXYCoordinates(x, y);
            freehandCrop.setStartCoordinates(new Point((int) x, (int) y));
        } else
        {
            invalid = true;
        }
    }

    /**
     * Handle the user's first touch on the canvas via the rectangle crop.
     * @param x - the x-ordinate of the touch.
     * @param y - the y-ordinate of the touch.
     */
    private void touchStartRectangle (float x, float y)
    {
        if (isWithinBitmap(x, y))
        {
            // create an artificial path which will work with hasPath() for correct back press functionality
            freehandCrop.createCropPath();
            // set the start and end coordinates
            rectangleCrop.setStartCoordinates(x, y);
            if (rectangleCrop.getEndX() == 0 || rectangleCrop.getEndY() == 0)
                rectangleCrop.setEndCoordinates(rectangleCrop.getStartX(), rectangleCrop.getStartY());
        } else
        {
            invalid = true;
        }
    }

    /**
     * Handle the movement performed after the first touch.
     * @param x - the x-ordinate of the touch.
     * @param y - the y-ordinate of the touch.
     */
    private void touchMove (float x, float y)
    {
        float oldX = freehandCrop.getX();
        float oldY = freehandCrop.getY();

        if (!invalid)
        {
            if (isWithinBitmap(x, y))
            {
                // move the crop path and update the coordinates
                freehandCrop.pathQuadTo(oldX, oldY, (x + oldX) / 2, (y + oldY) / 2);
                freehandCrop.cropPathQuadTo(oldX - bitmapLeft, oldY - bitmapTop,
                        ((x + oldX) / 2) - bitmapLeft, ((y + oldY) / 2) - bitmapTop);
                freehandCrop.setXYCoordinates(x, y);
            } else
            {
                // move the crop path such that it matches with the edge of the bitmap
                if (y < bitmapTop)
                {
                    freehandCrop.pathQuadTo(oldX, oldY, (x + oldX) / 2, bitmapTop);
                    freehandCrop.setXYCoordinates(x, bitmapTop);
                } else if (y > (bitmapTop + bitmapHandler.getScaledBitmap().getHeight()))
                {
                    freehandCrop.pathQuadTo(oldX, oldY, (x + oldX) / 2, (bitmapTop + bitmapHandler.getScaledBitmap().getHeight()));
                    freehandCrop.setXYCoordinates(x, bitmapTop + bitmapHandler.getScaledBitmap().getHeight());
                } else if (x < bitmapLeft)
                {
                    freehandCrop.pathQuadTo(oldX, oldY, bitmapLeft, (y + oldY) / 2);
                    freehandCrop.setXYCoordinates(bitmapLeft, y);
                } else if (x > (bitmapLeft + bitmapHandler.getScaledBitmap().getWidth()))
                {
                    freehandCrop.pathQuadTo(oldX, oldY, (bitmapLeft + bitmapHandler.getScaledBitmap().getWidth()), (y + oldY) / 2);
                    freehandCrop.setXYCoordinates(bitmapLeft + bitmapHandler.getScaledBitmap().getWidth(), y);
                }
            }
        }
    }

    /**
     * Handle the movement performed after the first touch via the rectangle crop.
     * @param x - the x-ordinate of the touch.
     * @param y - the y-ordinate of the touch.
     */
    private void touchMoveRectangle (float x, float y)
    {
        if (!invalid)
        {
            // continue if the crop size is adequate
            if (rectangleCrop.hasMinimumStrokeLength(x, y))
            {
                if (isWithinBitmap(x, y))
                {
                    // set the end of the crop
                    rectangleCrop.setEndCoordinates(x, y);
                } else
                {
                    // move the crop path such that it matches with the edge of the bitmap
                    if (y < bitmapTop)
                    {
                        rectangleCrop.setEndCoordinates(x, bitmapTop);
                    } else if (y > (bitmapTop + bitmapHandler.getScaledBitmap().getHeight()))
                    {
                        rectangleCrop.setEndCoordinates(x, bitmapTop + bitmapHandler.getScaledBitmap().getHeight());
                    } else if (x < bitmapLeft)
                    {
                        rectangleCrop.setEndCoordinates(bitmapLeft, y);
                    } else if (x > (bitmapLeft + bitmapHandler.getScaledBitmap().getWidth()))
                    {
                        rectangleCrop.setEndCoordinates(bitmapLeft + bitmapHandler.getScaledBitmap().getWidth(), y);
                    }
                }
            }
        }
    }

    /**
     * Handle the end of the touch by the user.
     */
    private void touchUp ()
    {
        if (!invalid)
        {
            // draw the path line to the most recent coordinate
            freehandCrop.pathLineTo(freehandCrop.getX(), freehandCrop.getY());
            freehandCrop.cropPathLineTo(freehandCrop.getX() - bitmapLeft, freehandCrop.getY() - bitmapTop);
            freehandCrop.setEndCoordinates(new Point((int) freehandCrop.getX(), (int) freehandCrop.getY()));
            // add the path to the list
            freehandCrop.addDrawPath(new DrawPath(Color.WHITE, 5, freehandCrop.getPath()));
            // draw the path line and the adjusted crop line
            freehandCrop.pathMoveTo(freehandCrop.getStart().x, freehandCrop.getStart().y);
            freehandCrop.cropPathMoveTo(freehandCrop.getStart().x, freehandCrop.getStart().y - bitmapTop);
            freehandCrop.pathLineTo(freehandCrop.getEnd().x, freehandCrop.getEnd().y);
            freehandCrop.cropPathLineTo(freehandCrop.getEnd().x, freehandCrop.getEnd().y - bitmapTop);
        }
        invalid = false;
    }

    /**
     * Draws the crop path when invalidate() is called.
     * @param canvas - the canvas which objects are drawn on.
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        // save the canvas and draw the transparent layer
        canvas.save();
        canvas.drawColor(0x00AAAAAA);

        if (bitmapHandler.getScaledBitmap() != null)
        {
            mCanvas = canvas;
            // set the left and top coordinates for the bitmap (i.e., with scaling)
            bitmapTop = Math.abs(getHeight() - bitmapHandler.getScaledBitmap().getHeight()) / 2;
            bitmapLeft = Math.abs(getWidth() - bitmapHandler.getScaledBitmap().getWidth()) / 2;
            // draw the scaled bitmap
            canvas.drawBitmap(bitmapHandler.getScaledBitmap(), bitmapLeft, bitmapTop, mPaint);
        }

        if (mCropType == 1)
        {
            // draw the crop rectangle
            canvas.drawRect(rectangleCrop.getLeft(), rectangleCrop.getTop(), rectangleCrop.getRight(),
                    rectangleCrop.getBottom(), mPaint);
        } else
        {
            // loop through the paths which hold the crop drawing information
            for (DrawPath draw : freehandCrop.getPathsList())
            {
                // set the paint information
                mPaint.setStrokeWidth(draw.getWidth());
                mPaint.setMaskFilter(null);
                mPaint.setColor(draw.getColour());
                mPaint.setColorFilter(null);
                // set the paint object to be used depending on the crop method
                if (mCropType == CROP_LASSO)
                {
                    canvas.drawPath(draw.getPath(), mLassoPaint);
                } else
                {
                    canvas.drawPath(draw.getPath(), mPaint);
                }
            }
        }
        canvas.restore();
    }
}