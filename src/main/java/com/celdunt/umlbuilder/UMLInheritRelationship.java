package com.celdunt.umlbuilder;

import com.intellij.ui.JBColor;

import java.awt.*;

public class UMLInheritRelationship extends UMLRelationship {

    @Override
    void defineUpArrow() {
        upArrow.xpoints = new int[] {endX - 5, endX + 5, endX, endX - 5};
        upArrow.ypoints = new int[] {endY, endY, endY + 10, endY};
        upArrow.npoints = 4;
    }
    @Override
    void defineDownArrow() {
        downArrow.xpoints = new int[] {endX - 5, endX + 5, endX, endX - 5};
        downArrow.ypoints = new int[] {endY, endY, endY - 10, endY};
        downArrow.npoints = 4;
    }
    @Override
    void defineLeftArrow() {
        leftArrow.xpoints = new int[] {endX, endX, endX - 10, endX};
        leftArrow.ypoints = new int[] {endY + 5, endY - 5, endY, endY + 5};
        leftArrow.npoints = 4;
    }
    @Override
    void defineRightArrow() {
        rightArrow.xpoints = new int[] {endX, endX, endX + 10, endX};
        rightArrow.ypoints = new int[] {endY + 5, endY - 5, endY, endY + 5};
        rightArrow.npoints = 4;
    }
}
