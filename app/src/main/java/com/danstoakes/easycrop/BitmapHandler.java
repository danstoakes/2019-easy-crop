package com.danstoakes.easycrop;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Helper class which stores the original and scaled versions of the bitmap, as well as helper methods.
 */
public class BitmapHandler
{
    private Bitmap unscaledBitmap;
    private Bitmap scaledBitmap;

    /**
     * Sets the unscaled version of the bitmap.
     * @param bitmap - the bitmap to set.
     */
    public void setUnscaledBitmap (Bitmap bitmap)
    {
        unscaledBitmap = bitmap;
    }

    /**
     * Returns the unscaled version of the bitmap.
     * @return bitmap - the bitmap to retrieve.
     */
    public Bitmap getUnscaledBitmap ()
    {
        return unscaledBitmap;
    }

    /**
     * Sets the scaled version of the bitmap.
     * @param bitmap - the bitmap to set.
     */
    public void setScaledBitmap (Bitmap bitmap)
    {
        scaledBitmap = bitmap;
    }

    /**
     * Returns the scaled version of the bitmap.
     * @return bitmap - the bitmap to retrieve.
     */
    public Bitmap getScaledBitmap ()
    {
        return scaledBitmap;
    }

    /**
     * Returns an input bitmap as a scaled representation.
     * @param bitmap - the input bitmap.
     * @param width - the width of the application window.
     * @param height - the height of the application window.
     * @return Bitmap - the scaled representation of the input bitmap.
     */
    public static Bitmap getAsScaledBitmap (Bitmap bitmap, int width, int height)
    {
        // calculate the ratio in width/height between the image and the window
        float ratio = Math.min(
                (float) width / bitmap.getWidth(),
                (float) height / bitmap.getHeight());
        // adjust the width/height values so that they are scaled
        int scaledWidth = Math.round(bitmap.getWidth() * ratio);
        int scaledHeight = Math.round(bitmap.getHeight() * ratio);
        // create a scaled bitmap using the new dimensions
        return Bitmap.createScaledBitmap(
                bitmap, scaledWidth, scaledHeight, true);
    }

    /**
     * Returns the matrix which can be applied to a bitmap to perform mirroring/flipping.
     * @param bitmap - the input bitmap.
     * @param centreX - the centre x-ordinate.
     * @param centreY - the center y-ordinate.
     * @return Matrix - the mirror/flip matrix.
     */
    public static Matrix getFlipMatrix(Bitmap bitmap, int centreX, int centreY)
    {
        Matrix matrix = new Matrix();
        if (bitmap.getHeight() > bitmap.getWidth())
        {
            // mirror in x if the bitmap is portrait
            matrix.postScale(-1, 1, centreX, centreY);
        } else
        {
            // mirror in y if the bitmap is landscape
            matrix.postScale(1, -1, centreX, centreY);
        }
        return matrix;
    }

    /**
     * Returns the flipped representation of an input bitmap.
     * @param bitmap - the input bitmap.
     * @return Bitmap - the flipped bitmap.
     */
    public static Bitmap flipBitmap (Bitmap bitmap)
    {
        // retrieve the dimensions of the bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // create a bitmap by passing in the flip matrix as a parameter
        return Bitmap.createBitmap(
                bitmap, 0, 0, width, height,
                getFlipMatrix(bitmap,width / 2, height / 2), true);
    }

    /**
     * Returns the matrix which can be applied to a bitmap to perform rotation.
     * @return Matrix - the rotation matrix.
     */
    public static Matrix getRotateMatrix()
    {
        // create a new matrix and rotate 90 degrees clockwise
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        // return the matrix
        return matrix;
    }

    /**
     * Returns the rotated representation of an input bitmap.
     * @param bitmap - the input bitmap.
     * @return Bitmap - the rotated bitmap.
     */
    public static Bitmap rotateBitmap (Bitmap bitmap)
    {
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), getRotateMatrix(), true);
    }
}