package com.celdunt.umlbuilder.relationships;

import java.awt.*;

public class UMLInnerRelationship extends UMLRelationship {

    public UMLInnerRelationship(int sizeArrow, int numeratorOfRelations, int denominatorOfRelations) {
        super(sizeArrow, numeratorOfRelations, denominatorOfRelations);
    }

    @Override
    void defineUpArrow() {
        upArrow.xpoints = new int[]{endX - sizeArrow, endX, endX - (int) (sizeArrow / 1.5), endX - (int) (sizeArrow / 1.5),
                endX + (int) (sizeArrow / 1.5), endX + (int) (sizeArrow / 1.5), endX, endX + sizeArrow, endX};
        upArrow.ypoints = new int[]{endY, endY, endY + sizeArrow / 2, endY + sizeArrow,
                endY + sizeArrow, endY + sizeArrow / 2, endY, endY, endY};
        upArrow.npoints = 9;
    }

    @Override
    void defineDownArrow() {
        downArrow.xpoints = new int[]{endX - sizeArrow, endX, endX - sizeArrow / 2, endX - sizeArrow / 2,
                endX + sizeArrow / 2, endX + sizeArrow / 2, endX, endX + sizeArrow, endX};
        downArrow.ypoints = new int[]{endY, endY, endY - sizeArrow / 2, endY - sizeArrow,
                endY - sizeArrow, endY - sizeArrow / 2, endY, endY, endY};
        downArrow.npoints = 9;
    }

    @Override
    void defineLeftArrow() {
        leftArrow.xpoints = new int[]{endX - sizeArrow, endX - sizeArrow / 2, endX, endX, endX,
                endX, endX - sizeArrow / 2, endX - sizeArrow, endX - sizeArrow};
        leftArrow.ypoints = new int[]{endY - sizeArrow / 2, endY - sizeArrow / 2, endY, endY - sizeArrow, endY + sizeArrow, endY,
                endY + sizeArrow / 2, endY + sizeArrow / 2, endY - sizeArrow / 2};
        leftArrow.npoints = 9;
    }

    @Override
    void defineRightArrow() {
        rightArrow.xpoints = new int[]{endX + sizeArrow, endX + sizeArrow / 2, endX, endX, endX,
                endX, endX + sizeArrow / 2, endX + sizeArrow, endX + sizeArrow};
        rightArrow.ypoints = new int[]{endY - sizeArrow / 2, endY - sizeArrow / 2, endY, endY - sizeArrow, endY + sizeArrow, endY,
                endY + sizeArrow / 2, endY + sizeArrow / 2, endY - sizeArrow / 2};
        rightArrow.npoints = 9;
    }

    @Override
    void defineStrokeArrow() {
        strokeArrow = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{10f, 10f}, 0f);
    }
}
