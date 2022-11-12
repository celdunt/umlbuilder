package com.celdunt.umlbuilder.relationships;

import java.awt.*;

public class UMLDependenceRelationship extends UMLInheritRelationship {
    public UMLDependenceRelationship(int sizeArrow, int n, int m) {
        super(sizeArrow, n, m);
    }
    @Override
    void defineStrokeArrow() {
        strokeArrow = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{10f, 10f}, 0f);
    }
}
