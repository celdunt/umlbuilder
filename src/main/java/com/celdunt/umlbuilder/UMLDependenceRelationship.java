package com.celdunt.umlbuilder;

import java.awt.*;

public class UMLDependenceRelationship extends UMLInheritRelationship {
    @Override
    void defineStrokeArrow() {
        strokeArrow = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{10f, 10f}, 0f);
    }
}
