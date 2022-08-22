package com.moviera.vision;

public class VisionFilter
{
    public static final int VISION_FILTER_NONE = 0;
    public static final int VISION_FILTER_LIPSTICK = 1;
    public static final int VISION_FILTER_EYEBROWS = 2;
    public static final int VISION_FILTER_LINECONTOURS = 3;
    public static final int VISION_FILTER_DOTCONTOURS = 4;
    public static final int VISION_FILTER_LETTERBOX = 5;
    public static final int VISION_FILTER_COLOREDEYES = 6;

    private boolean fill = true, stroke = false;
    private int filter, fillColor, strokeColor, strokeWidth = 0;

    public VisionFilter()
    {
        this.fillColor = VisionColors.NULL;
        this.strokeColor = VisionColors.NULL;
        this.filter = VISION_FILTER_NONE;
    }

    public VisionFilter(int fillColor, int strokeColor, int filter, int strokeWidth, boolean fill, boolean stroke)
    {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.filter = filter;
        this.strokeWidth = strokeWidth;
        this.fill = fill;
        this.stroke = stroke;
    }

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStroke(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public boolean getStroke() {
        return stroke;
    }

    public void setStroke(boolean stroke) {
        this.stroke = stroke;
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }
}
