package com.danstoakes.easycrop;

import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;

/**
 * Helper class which holds the values and methods for any cropping completed
 * using the Freehand crop type.
 */
public class FreehandCrop
{
    private float x, y;

    private Point start;
    private Point end;

    private Path path;
    private Path cropPath;

    private final ArrayList<DrawPath> paths;

    /**
     * Constructor for the FreehandCrop class.
     */
    public FreehandCrop ()
    {
        // initialise the list of paths
        paths = new ArrayList<>();
    }

    /**
     * Sets the x,y coordinates for the Freehand crop type.
     * @param x - the x-ordinate.
     * @param y - the y-ordinate.
     */
    public void setXYCoordinates (float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Adds a DrawPath object to the list of existing paths.
     * @param drawPath - the DrawPath object to add.
     */
    public void addDrawPath (DrawPath drawPath)
    {
        paths.add(drawPath);
    }

    /**
     * Gets the start coordinates for the path.
     * @return start - the start coordinates.
     */
    public Point getStart()
    {
        return start;
    }

    /**
     * Gets the end coordinates for the path.
     * @return end - the end coordinates.
     */
    public Point getEnd ()
    {
        return end;
    }

    /**
     * Gets the most recent x-ordinate for the path.
     * @return x - the x-ordinate.
     */
    public float getX ()
    {
        return x;
    }

    /**
     * Gets the most recent y-ordinate for the path.
     * @return y - the y-ordinate.
     */
    public float getY ()
    {
        return y;
    }

    /**
     * Returns the list of paths to be drawn upon the canvas.
     * @return paths - the list of paths to be drawn.
     */
    public ArrayList<DrawPath> getPathsList ()
    {
        return paths;
    }

    /**
     * Clears the paths list.
     */
    public void clearPathsList ()
    {
        paths.clear();
    }

    /**
     * Gets the path of the path drawn upon the canvas.
     * @return Path - the path drawn upon the canvas.
     */
    public Path getPath ()
    {
        return path;
    }

    public void createCropPath ()
    {
        cropPath = new Path();
    }

    /**
     * Gets the path of the path to be used for cropping.
     * @return Path - the path to be used for cropping.
     */
    public Path getCropPath ()
    {
        return cropPath;
    }

    /**
     * Creates new path and cropPath objects.
     */
    public void createPaths ()
    {
        path = new Path();
        cropPath = new Path();
    }

    /**
     * Clears the existing path and cropPath objects.
     */
    public void resetPaths ()
    {
        path.reset();
        cropPath.reset();
    }

    /**
     * Deletes the existing path and cropPath objects.
     */
    public void emptyPaths ()
    {
        path = null;
        cropPath = null;
    }

    /**
     * Sets the start coordinates.
     * @param start - the start coordinates.
     */
    public void setStartCoordinates (Point start)
    {
        this.start = start;
    }

    /**
     * Sets the end coordinates.
     * @param end - the end coordinates.
     */
    public void setEndCoordinates (Point end)
    {
        this.end = end;
    }

    /**
     * Performs a lineTo operation on the path object.
     * @param x - the x-ordinate to lineTo.
     * @param y - the y-ordinate to lineTo.
     */
    public void pathLineTo (float x, float y)
    {
        path.lineTo(x, y);
    }

    /**
     * Performs a moveTo operation on the path object.
     * @param x - the x-ordinate to moveTo.
     * @param y - the y-ordinate to moveTo.
     */
    public void pathMoveTo (float x, float y)
    {
        path.moveTo(x, y);
    }

    /**
     * Performs a quadTo operation on the path object.
     * @param x1 - the first x-ordinate to moveTo.
     * @param y1 - the first y-ordinate to moveTo.
     * @param x2 - the second x-ordinate to moveTo.
     * @param y2 - the second y-ordinate to moveTo.
     */
    public void pathQuadTo (float x1, float y1, float x2, float y2)
    {
        path.quadTo(x1, y1, x2, y2);
    }

    /**
     * Performs a lineTo operation on the cropPath object.
     * @param x - the x-ordinate to lineTo.
     * @param y - the y-ordinate to lineTo.
     */
    public void cropPathLineTo (float x, float y)
    {
        cropPath.lineTo(x, y);
    }

    /**
     * Performs a moveTo operation on the cropPath object.
     * @param x - the x-ordinate to moveTo.
     * @param y - the y-ordinate to moveTo.
     */
    public void cropPathMoveTo (float x, float y)
    {
        cropPath.moveTo(x, y);
    }

    /**
     * Performs a quadTo operation on the cropPath object.
     * @param x1 - the first x-ordinate to moveTo.
     * @param y1 - the first y-ordinate to moveTo.
     * @param x2 - the second x-ordinate to moveTo.
     * @param y2 - the second y-ordinate to moveTo.
     */
    public void cropPathQuadTo (float x1, float y1, float x2, float y2)
    {
        cropPath.quadTo(x1, y1, x2, y2);
    }
}