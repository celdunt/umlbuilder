package com.celdunt.umlbuilder;

import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class UMLClass extends UMLFigure {
    public enum ClassType {
        CLASS,
        ABSTRACT,
        INTERFACE,
        ENUM
    }
    private final int widthOval = 25;
    private final int heightOval = 25;
    private final int margin = 6;

    private ClassType type = ClassType.CLASS;
    private String name = "Default";
    public UMLRelationship.LinkType linkType = UMLRelationship.LinkType.NONE;
    private final ArrayList<String> fields = new ArrayList<>();
    private final ArrayList<String> methods = new ArrayList<>();
    private final ArrayList<UMLClass> parents = new ArrayList<>();
    private final ArrayList<UMLClass> children = new ArrayList<>();

    private PsiJavaCodeReferenceElement[] rawExtends;
    private PsiJavaCodeReferenceElement[] rawImplements;


    private final Font font = new Font("Arial", Font.PLAIN, 12);
    private final FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), true, true);


    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(JBColor.black);

        calculateSizeClassRectangle();

        g2d.drawRect(this.x, this.y, (int) this.width, (int) this.height);

        String abstractHexCode = "#5d8aa8";
        String classHexCode = "#915c83";
        String interfaceHexCode = "#9966cc";
        String enumHexCode = "#9855cc";

        switch (type) {
            case CLASS:
                drawTypeClassCircle(g2d, "C", classHexCode);
                break;
            case ABSTRACT:
                drawTypeClassCircle(g2d, "A", abstractHexCode);
                break;
            case INTERFACE:
                drawTypeClassCircle(g2d, "I", interfaceHexCode);
                break;
            case ENUM:
                drawTypeClassCircle(g2d, "@e", enumHexCode);
        }

        drawContentClass(g2d);
    }

    public void linkClass(UMLClass out, UMLRelationship relationship, Graphics2D g2d) {
        int inaccuracy = 150;

        if (out.x > this.x && out.y - this.y <= Math.abs(inaccuracy)) initializeRelationship(out, this, relationship, UMLRelationship.ArrowDirection.LEFT);
        else if (this.x > out.x && out.y - this.y <= Math.abs(inaccuracy)) initializeRelationship(out, this, relationship, UMLRelationship.ArrowDirection.RIGHT);
        else if (out.y > this.y) initializeRelationship(out, this, relationship, UMLRelationship.ArrowDirection.DOWN);
        else if (this.y > out.y) initializeRelationship(out, this, relationship, UMLRelationship.ArrowDirection.UP);

        relationship.draw(g2d);
    }
    private void initializeRelationship(UMLClass first, UMLClass second, UMLRelationship relationship, UMLRelationship.ArrowDirection arrowDirection) {
        UMLClass a = new UMLClass()
                .defX(first.x)
                .defY(first.y)
                .defWidth(first.width)
                .defHeight(first.height);
        UMLClass b = new UMLClass()
                .defX(second.x)
                .defY(second.y)
                .defWidth(second.width)
                .defHeight(second.height);

        switch (arrowDirection) {
            case LEFT:
                b.x += b.width + relationship.sizeArrow;
                a.y += a.height / (relationship.m+1) * relationship.n;
                b.y += b.height / (relationship.m+1) * relationship.n;
                test(a, b, "LEFT");
                break;
            case RIGHT:
                a.x += a.width;
                b.x -= relationship.sizeArrow;
                a.y += a.height / (relationship.m+1) * relationship.n;
                b.y += b.height / (relationship.m+1) * relationship.n;
                test(a, b, "RIGHT");
                break;
            case DOWN:
                b.y += b.height + relationship.sizeArrow;
                a.x += a.width / (relationship.m+1) * relationship.n;
                b.x += b.width / (relationship.m+1) * relationship.n;
                test(a, b, "DOWN");
                break;
            case UP:
                a.y += a.height;
                b.y -= relationship.sizeArrow;
                a.x += a.width / (relationship.m+1) * relationship.n;
                b.x += b.width / (relationship.m+1) * relationship.n;
                test(a, b, "UP");
                break;
        }

        relationship.defStartX(a.x)
                .defStartY(a.y)
                .defEndX(b.x)
                .defEndY(b.y)
                .defArrowDirection(arrowDirection);
    }

    private int getTextWidth(String text) {
        return (int) font.getStringBounds(text, fontRenderContext).getWidth();
    }

    private int getTextHeight(String text) {
        return (int) font.getStringBounds(text, fontRenderContext).getHeight();
    }

    public UMLClass calculateSizeClassRectangle() {
        AtomicInteger widthFields = new AtomicInteger(0);
        AtomicInteger heightFields = new AtomicInteger(0);

        calculateMaxSize(widthFields, heightFields, fields);

        AtomicInteger widthMethods = new AtomicInteger(0);
        AtomicInteger heightMethods = new AtomicInteger(0);

        calculateMaxSize(widthMethods, heightMethods, methods);

        int widthContent = Integer.max(widthFields.get(), widthMethods.get());
        int heightContent = heightFields.get() + heightMethods.get();

        if (getTextWidth(name) > widthContent) widthContent = getTextWidth(name) + margin;

        if (widthOval + 2 * margin + widthContent > this.width)
            this.width = widthOval + 4 * margin + widthContent;

        if (heightOval + 2 * margin + getTextHeight(name) + heightContent > this.height)
            this.height = heightOval + 5 * margin + getTextHeight(name) + heightContent;

        return this;
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

        currentY.addAndGet(2 * margin);

        drawContentClassStrings(g2d, currentY, methods);
    }

    public UMLRelationship getLinkClass(int sizeArrow, int n, int m) {
        switch (this.linkType) {
            case INHERIT: return new UMLInheritRelationship(sizeArrow, n, m);
            case DEPENDENCE: return new UMLDependenceRelationship(sizeArrow, n, m);
            default: return new UMLInheritRelationship(0, 0, 0);
        }
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

    public UMLClass defX(int x) {
        this.x = x;
        return this;
    }

    public UMLClass defY(int y) {
        this.y = y;
        return this;
    }

    public UMLClass defType(ClassType type) {
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
    public void defRawExtends(PsiJavaCodeReferenceElement[] rawExtends) {
        this.rawExtends = rawExtends;
    }
    public void defRawImplements(PsiJavaCodeReferenceElement[] rawImplements) {
        this.rawImplements = rawImplements;
    }

    public PsiJavaCodeReferenceElement[] getRawExtends() {
        return rawExtends;
    }
    public PsiJavaCodeReferenceElement[] getRawImplements() {
        return rawImplements;
    }

    public void addParent(UMLClass parent) {
        if (!parents.contains(parent))
            parents.add(parent);
    }
    public UMLClass addChild(UMLClass child) {
        if (!children.contains(child))
            children.add(child);
        return this;
    }

    public ArrayList<UMLClass> getParents() {
        return parents;
    }
    public ArrayList<UMLClass> getChildren() {
        return children;
    }
    public String getName() {
        return name;
    }

    ///test
    private void test(UMLClass a, UMLClass b, String testMessage) {
        System.out.println(testMessage + "\n" + a.x + " " + a.y + "<--Class A\n" +
                b.x + " " + b.y + "<--Class B");
    }
}
