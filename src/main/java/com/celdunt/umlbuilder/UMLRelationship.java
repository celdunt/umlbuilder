package com.celdunt.umlbuilder;

import java.awt.*;

public abstract class UMLRelationship extends UMLFigure {

    public enum ArrowDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public UMLRelationship(int sizeArrow, int n, int m) {
        this.sizeArrow = sizeArrow;
        this.n = n;
        this.m = m;
    }

    protected int endX = 0;
    protected int endY = 0;
    protected int sizeArrow;
    protected int n; // <--- дать нормальное название
    protected int m; // <--- дать нормальное название

    protected final Polygon upArrow = new Polygon();
    protected final Polygon downArrow = new Polygon();
    protected final Polygon leftArrow = new Polygon();
    protected final Polygon rightArrow = new Polygon();
    protected BasicStroke strokeArrow = new BasicStroke();
    protected ArrowDirection arrowDirection = ArrowDirection.LEFT;

    @Override
    public void draw(Graphics2D g2d) {
        defineUpArrow();
        defineDownArrow();
        defineLeftArrow();
        defineRightArrow();
        defineStrokeArrow();

        g2d.setColor(this.color);

        g2d.setStroke(strokeArrow);

        g2d.drawLine(x, y, endX, endY);

        g2d.setStroke(new BasicStroke(1));

        calculateArrowDirection(g2d);
    }
    private void calculateArrowDirection(Graphics2D g2d) {
        switch (arrowDirection) {
            case LEFT: g2d.drawPolygon(leftArrow); break;
            case RIGHT: g2d.drawPolygon(rightArrow); break;
            case UP: g2d.drawPolygon(upArrow); break;
            case DOWN: g2d.drawPolygon(downArrow); break;
        }
    }

    abstract void defineUpArrow();
    abstract void defineDownArrow();
    abstract void defineLeftArrow();
    abstract void defineRightArrow();
    abstract void defineStrokeArrow();

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
    public UMLRelationship defArrowDirection(ArrowDirection arrowDirection) {
        this.arrowDirection = arrowDirection;
        return this;
    }
}
