package com.celdunt.umlbuilder;

import com.intellij.ui.JBColor;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class UMLClass extends UMLFigure {
    public static final String abstractType = "A";
    public static final String classType = "C";
    public static final String interfaceType = "I";

    private final String abstractHexCode = "#5d8aa8";
    private final String classHexCode = "#915c83";
    private final String interfaceHexCode = "#9966cc";

    private int widthOval = 25;
    private int heightOval = 25;
    private int margin = 6;

    private String type = classType;
    private String name = "Default";
    private ArrayList<String> fields = new ArrayList<>();
    private ArrayList<String> methods = new ArrayList<>();


    private final Font font = new Font("Arial", Font.PLAIN, 10);
    private final FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), true, true);


    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(JBColor.black);

        calculateSizeClassRectangle();

        g2d.drawRect(this.x, this.y, (int) this.width, (int) this.height);

        switch (type) {
            case classType:
                drawTypeClassCircle(g2d, type, classHexCode);
            case abstractType:
                drawTypeClassCircle(g2d, type, abstractHexCode);
            case interfaceType:
                drawTypeClassCircle(g2d, type, interfaceHexCode);
        }

        drawContentClass(g2d);
    }

    public void linkClass(UMLClass out, UMLInheritRelationship relationship, Graphics2D g2d) {
        //fix
        relationship.defStartX(out.x + (int)out.width)
                .defStartY(out.y + (int)out.height/2)
                .defEndX(this.x - 10)
                .defEndY(this.y + (int)this.height/2)
                .draw(g2d);
    }

    private int getTextWidth(String text) {
        return (int) font.getStringBounds(text, fontRenderContext).getWidth();
    }

    private int getTextHeight(String text) {
        return (int) font.getStringBounds(text, fontRenderContext).getHeight();
    }

    private void calculateSizeClassRectangle() {
        AtomicInteger widthFields = new AtomicInteger(0);
        AtomicInteger heightFields = new AtomicInteger(0);

        calculateMaxSize(widthFields, heightFields, fields);

        AtomicInteger widthMethods = new AtomicInteger(0);
        AtomicInteger heightMethods = new AtomicInteger(0);

        calculateMaxSize(widthMethods, heightMethods, methods);

        int widthContent = Integer.max(widthFields.get(), widthMethods.get());
        int heightContent = Integer.max(heightFields.get(), heightMethods.get());

        if (getTextWidth(name) > widthContent) widthContent = getTextWidth(name) + margin;

        if (widthOval + 2 * margin + widthContent > this.width)
            this.width = widthOval + 4 * margin + widthContent;

        if (heightOval + 2 * margin + getTextHeight(name) + heightContent > this.height)
            this.height = heightOval + 5 * margin + getTextHeight(name) + heightContent;
    }

    private void calculateMaxSize(AtomicInteger width, AtomicInteger height, ArrayList<String> listString) {
        for (String item : listString) {
            height.addAndGet(getTextHeight(item));
            width.set(Integer.max(width.get(), getTextWidth(item)));
        }
    }

    private void drawTypeClassCircle(Graphics2D g2d, String type, String hexCode) {
        g2d.setColor(Color.decode(hexCode));
        g2d.fillOval(this.x + margin, this.y + margin, widthOval, heightOval);

        g2d.setColor(JBColor.black);

        final int posInnerX = -getTextWidth(type) / 2 + margin - 1;
        final int posInnerY = -getTextHeight(type) / 2 + margin / 2 + 1;

        g2d.drawString(type, this.x + widthOval / 2 + posInnerX, this.y + getTextHeight(type) + heightOval / 2 + posInnerY);
        g2d.drawString(name, this.x + margin + widthOval + margin, this.y + getTextHeight(name) + 2 * margin);
    }

    private void drawContentClass(Graphics2D g2d) {
        AtomicInteger currentY = new AtomicInteger(this.y + 2 * heightOval);

        g2d.setColor(JBColor.black);

        drawContentClassStrings(g2d, currentY, fields);

        currentY.addAndGet(margin);

        drawContentClassStrings(g2d, currentY, methods);
    }

    private void drawContentClassStrings(Graphics2D g2d, AtomicInteger currentY, ArrayList<String> listString) {
        for (String item : listString) {
            g2d.drawString(item, (int) (this.x + this.width / 2 - getTextWidth(item) / 2), currentY.get());
            currentY.addAndGet(getTextHeight(item));
        }
    }

    public UMLClass defWidth(double width) {
        this.width = width;
        return this;
    }

    public UMLClass defHeight(double height) {
        this.height = height;
        return this;
    }

    public UMLClass defColor(Color color) {
        this.color = color;
        return this;
    }

    public UMLClass defX(int x) {
        this.x = x;
        return this;
    }

    public UMLClass defY(int y) {
        this.y = y;
        return this;
    }

    public UMLClass defType(String type) {
        if (Objects.equals(type, UMLClass.classType) ||
                Objects.equals(type, UMLClass.abstractType) ||
                Objects.equals(type, UMLClass.interfaceType))
            this.type = type;
        return this;
    }

    public UMLClass defName(String name) {
        this.name = name;
        return this;
    }

    public UMLClass defFields(ArrayList<String> fields) {
        this.fields.addAll(fields);
        return this;
    }

    public UMLClass defMethods(ArrayList<String> methods) {
        this.methods.addAll(methods);
        return this;
    }
}