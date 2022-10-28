package com.celdunt.umlbuilder;

import com.intellij.ui.JBColor;

import java.awt.*;

public abstract class UMLFigure {
    double width = 0;
    double height = 0;
    Color color = JBColor.black;
    int x = 0;
    int y = 0;

    abstract void draw(Graphics2D g2d);
}
