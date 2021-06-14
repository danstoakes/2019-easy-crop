package com.danstoakes.easycrop;

/**
 * Helper class which holds the values and methods for any cropping completed
 * using the Classic crop type.
 */
public class RectangleCrop
{
    private static final int MINIMUM_STROKE_WIDTH = 10;

    private float startX, endX;
    private float startY, endY;

    /**
     * Constructor for the RectangleCrop class.
     */
    public RectangleCrop ()
    {
        clearValues();
    }

    /**
     * Sets the start coordinates.
     * @param x - the x-ordinate.
     * @param y - the y-ordinate.
     */
    public void setStartCoordinates (float x, float y)
    {
        startX = x;
        startY = y;
    }

    /**
     * Sets the end coordinates.
     * @param x - the x-ordinate.
     * @param y - the y-ordinate.
     */
    public void setEndCoordinates (float x, float y)
    {
        endX = x;
        endY = y;
    }

    /**
     * Returns the x-ordinate from the start coordinates.
     * @return startX - the x-ordinate.
     */
    public float getStartX ()
    {
        return startX;
    }

    /**
     * Returns the x-ordinate from the end coordinates.
     * @return endX - the x-ordinate.
     */
    public float getEndX ()
    {
        return endX;
    }

    /**
     * Returns the y-ordinate from the start coordinates.
     * @return startY - the y-ordinate.
     */
    public float getStartY ()
    {
        return startY;
    }

    /**
     * Returns the y-ordinate from the end coordinates.
     * @return endY - the y-ordinate.
     */
    public float getEndY ()
    {
        return endY;
    }

    /**
     * Returns the coordinate for the left corner of the rectangle to crop with.
     * @return float - the left corner.
     */
    public float getLeft ()
    {
        return Math.min(startX, endX);
    }

    /**
     * Returns the coordinate for the top of the rectangle to crop with.
     * @return float - the top of the rectangle.
     */
    public float getTop ()
    {
        return Math.min(startY, endY);
    }

    /**
     * Returns the coordinate for the right corner of the rectangle to crop with.
     * @return float - the right corner.
     */
    public float getRight ()
    {
        return Math.max(endX, startX);
    }

    /**
     * Returns the coordinate for the bottom of the rectangle to crop with.
     * @return float - the bottom of the rectangle.
     */
    public float getBottom ()
    {
        return Math.max(endY, startY);
    }

    /**
     * Returns the width of the rectangle to crop with.
     * @return float - the width.
     */
    public float getWidth ()
    {
        return Math.abs(endX - startX);
    }

    /**
     * Returns the height of the rectangle to crop with.
     * @return float - the height.
     */
    public float getHeight ()
    {
        return Math.abs(endY - startY);
    }

    /**
     * Returns whether the size of the rectangle is big enough.
     * @return boolean - whether the size is big enough.
     */
    public boolean hasMinimumStrokeLength ()
    {
        return Math.abs(startX - endX) > MINIMUM_STROKE_WIDTH || Math.abs(startY - endY) > MINIMUM_STROKE_WIDTH;
    }

    /**
     * Returns whether the size of the rectangle is big enough.
     * @param x - the x-ordinate to compare against.
     * @param y - the y-ordinate to compare against.
     * @return boolean - whether the size is big enough.
     */
    public boolean hasMinimumStrokeLength (float x, float y)
    {
        return Math.abs(x - endX) > MINIMUM_STROKE_WIDTH || Math.abs(y - endY) > MINIMUM_STROKE_WIDTH;
    }

    /**
     * Clears all coordinate values for the class.
     */
    public void clearValues ()
    {
        startX = 0;
        endX = 0;
        startY = 0;
        endY = 0;
    }
}