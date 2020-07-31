package com.example.paintapplication;

import android.graphics.Path;

public class FingerPath {
    public int color;
    public boolean emboss;
    public boolean blur;
    public boolean circle;
    public int strokeWidth;
    public Path path;

    public FingerPath(int color, boolean emboss, boolean blur, boolean circle, int strokeWidth, Path path) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.circle = circle;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
