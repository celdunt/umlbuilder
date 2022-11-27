package com.celdunt.umlbuilder.relationships;

import java.awt.*;

public class UMLAssociationRelationship extends UMLRelationship {

    public UMLAssociationRelationship(int sizeArrow, int numeratorOfRelations, int denominatorOfRelations) {
        super(sizeArrow, numeratorOfRelations, denominatorOfRelations);
    }

    @Override
    void defineUpArrow() {
        upArrow.xpoints = new int[] {endX, endX - sizeArrow/2, endX, endX + sizeArrow/2, endX};
        upArrow.ypoints = new int[] {endY + sizeArrow, endY - sizeArrow/2, endY + sizeArrow, endY - sizeArrow/2, endY + sizeArrow};
        upArrow.npoints = 5;
    }

    @Override
    void defineDownArrow() {
        downArrow.xpoints = new int[] {endX, endX - sizeArrow/2, endX, endX + sizeArrow/2, endX};
        downArrow.ypoints = new int[] {endY - sizeArrow, endY + sizeArrow/2, endY - sizeArrow, endY + sizeArrow/2, endY - sizeArrow};
        downArrow.npoints = 5;
    }

    @Override
    void defineLeftArrow() {
        leftArrow.xpoints = new int[] {endX - sizeArrow, endX + sizeArrow/2, endX - sizeArrow, endX + sizeArrow/2, endX - sizeArrow};
        leftArrow.ypoints = new int[] {endY, endY + sizeArrow/2, endY, endY - sizeArrow/2, endY};
        leftArrow.npoints = 5;
    }

    @Override
    void defineRightArrow() {
        rightArrow.xpoints = new int[] {endX + sizeArrow, endX - sizeArrow/2, endX + sizeArrow, endX - sizeArrow/2, endX + sizeArrow};
        rightArrow.ypoints = new int[] {endY, endY + sizeArrow/2, endY, endY - sizeArrow/2, endY};
        rightArrow.npoints = 5;
    }

    @Override
    void defineStrokeArrow() {
        strokeArrow = new BasicStroke(1);
    }
}
