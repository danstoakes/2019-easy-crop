package com.danstoakes.easycrop;

import android.graphics.Path;

public class Draw {

    private int colour;
    private int width;

    private Path path;

    public Draw(int colour, int width, Path path) {
        this.colour = colour;
        this.width = width;
        this.path = path;
    }

    public int getColour() {
        return colour;
    }

    public int getWidth() {
        return width;
    }

    public Path getPath() {
        return path;
    }
}