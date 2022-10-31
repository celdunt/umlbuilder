package com.celdunt.umlbuilder;

import com.intellij.ui.JBColor;

import java.awt.*;

public class UMLInheritRelationship extends UMLRelationship {
    public UMLInheritRelationship(int sizeArrow) {
        super(sizeArrow);
    }

    @Override
    void defineUpArrow() {
        upArrow.xpoints = new int[]{endX - sizeArrow / 2, endX + sizeArrow / 2, endX, endX - sizeArrow / 2};
        upArrow.ypoints = new int[]{endY, endY, endY + sizeArrow, endY};
        upArrow.npoints = 4;
    }

    @Override
    void defineDownArrow() {
        downArrow.xpoints = new int[]{endX - sizeArrow / 2, endX + sizeArrow / 2, endX, endX - sizeArrow / 2};
        downArrow.ypoints = new int[]{endY, endY, endY - sizeArrow, endY};
        downArrow.npoints = 4;
    }

    @Override
    void defineLeftArrow() {
        leftArrow.xpoints = new int[]{endX, endX, endX - sizeArrow, endX};
        leftArrow.ypoints = new int[]{endY + sizeArrow / 2, endY - sizeArrow / 2, endY, endY + sizeArrow / 2};
        leftArrow.npoints = 4;
    }

    @Override
    void defineRightArrow() {
        rightArrow.xpoints = new int[]{endX, endX, endX + sizeArrow, endX};
        rightArrow.ypoints = new int[]{endY + sizeArrow / 2, endY - sizeArrow / 2, endY, endY + sizeArrow / 2};
        rightArrow.npoints = 4;
    }

    @Override
    void defineStrokeArrow() {
        strokeArrow = new BasicStroke(1);
    }
}
