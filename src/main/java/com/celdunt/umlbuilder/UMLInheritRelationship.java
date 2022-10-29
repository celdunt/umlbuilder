package com.celdunt.umlbuilder;

import com.intellij.ui.JBColor;

import java.awt.*;

public class UMLInheritRelationship extends UMLFigure {
    private int endX = 0;
    private int endY = 0;

    private final Polygon upArrow = new Polygon();
    private final Polygon downArrow = new Polygon();
    private final Polygon leftArrow = new Polygon();
    private final Polygon rightArrow = new Polygon();

    @Override
    void draw(Graphics2D g2d) {
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


    private void defineUpArrow() {
        upArrow.xpoints = new int[] {endX - 5, endX + 5, endX, endX - 5};
        upArrow.ypoints = new int[] {endY, endY, endY + 10, endY};
        upArrow.npoints = 4;
    }
    private void defineDownArrow() {
        downArrow.xpoints = new int[] {endX - 5, endX + 5, endX, endX - 5};
        downArrow.ypoints = new int[] {endY, endY, endY - 10, endY};
        downArrow.npoints = 4;
    }
    private void defineLeftArrow() {
        leftArrow.xpoints = new int[] {endX, endX, endX - 10, endX};
        leftArrow.ypoints = new int[] {endY + 5, endY - 5, endY, endY + 5};
        leftArrow.npoints = 4;
    }
    private void defineRightArrow() {
        rightArrow.xpoints = new int[] {endX, endX, endX + 10, endX};
        rightArrow.ypoints = new int[] {endY + 5, endY - 5, endY, endY + 5};
        rightArrow.npoints = 4;
    }

    public UMLInheritRelationship defStartX(int startX) {
        this.x = startX;
        return this;
    }
    public UMLInheritRelationship defStartY(int startY) {
        this.y = startY;
        return this;
    }
    public UMLInheritRelationship defEndX(int endX) {
        this.endX = endX;
        return this;
    }
    public UMLInheritRelationship defEndY(int endY) {
        this.endY = endY;
        return this;
    }
}
