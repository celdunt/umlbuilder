package com.celdunt.umlbuilder;

import java.awt.*;

public abstract class UMLRelationship extends UMLFigure {

    protected int endX = 0;
    protected int endY = 0;

    protected final Polygon upArrow = new Polygon();
    protected final Polygon downArrow = new Polygon();
    protected final Polygon leftArrow = new Polygon();
    protected final Polygon rightArrow = new Polygon();

    @Override
    public void draw(Graphics2D g2d) {
        defineUpArrow();
        defineDownArrow();
        defineLeftArrow();
        defineRightArrow();

        final int inaccuracy = 100;

        g2d.setColor(this.color);
        g2d.drawLine(x, y, endX, endY);

        if (x > endX && y - endY <= Math.abs(inaccuracy)) g2d.drawPolygon(leftArrow);
        else if (endX > x && y - endY <= Math.abs(inaccuracy)) g2d.drawPolygon(rightArrow);
        else if (y > endY && x - endX <= Math.abs(inaccuracy)) g2d.drawPolygon(upArrow);
        else if (endY > y && x - endX <= Math.abs(inaccuracy)) g2d.drawPolygon(downArrow);
    }

    abstract void defineUpArrow();
    abstract void defineDownArrow();
    abstract void defineLeftArrow();
    abstract void defineRightArrow();

    public UMLRelationship defStartX(int startX) {
        this.x = startX;
        return this;
    }
    public UMLRelationship defStartY(int startY) {
        this.y = startY;
        return this;
    }
    public UMLRelationship defEndX(int endX) {
        this.endX = endX;
        return this;
    }
    public UMLRelationship defEndY(int endY) {
        this.endY = endY;
        return this;
    }
}
