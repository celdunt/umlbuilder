package com.celdunt.umlbuilder.figures;

import java.awt.*;

public abstract class UMLFigure {
    protected double width = 0;
    protected double height = 0;
    protected int x = 0;
    protected int y = 0;

    protected abstract void draw(Graphics2D g2d);

    public double getWidth() {
        return width;
    }
    public double getHeight() {
        return height;
    }
}
