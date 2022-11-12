package com.celdunt.umlbuilder.actions;

import com.celdunt.umlbuilder.figures.UMLClass;
import com.celdunt.umlbuilder.relationships.UMLRelationship;
import com.celdunt.umlbuilder.wrapers.WindowSize;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.util.ui.ImageUtil.createImage;

public class UMLBuilderAction extends AnAction {
    //РАЗОБРАТЬСЯ С ПЕРЕСЕЧЕНИЯМИ СТРЕЛОК
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object[] selectedClasses = e.getRequiredData(PlatformDataKeys.SELECTED_ITEMS);

        ArrayList<UMLClass> umlClasses = getUMLClassesFromPsiClasses(selectedClasses);

        structureUmlClasses(umlClasses);

        WindowSize windowSize = calculateWindowSize(umlClasses);

        BufferedImage bufferedImage = createImage(windowSize.width, windowSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = createGraphics(bufferedImage, windowSize.width, windowSize.height);

        drawUML(umlClasses, g2d, windowSize);

        VirtualFile chooserVFile = getChooserVirtualFile(e);

        BufferedImage resultBufferedImage = bufferedImage.getSubimage(0, 0, windowSize.width, windowSize.height);

        if (chooserVFile != null) saveGraphicsAsImage(resultBufferedImage, chooserVFile.getPath());
    }

    private void drawUML(ArrayList<UMLClass> umlClasses, Graphics2D g2d, WindowSize newSize) {
        AtomicInteger coordinateX = new AtomicInteger(5);
        int coordinateY = 5;
        int maxWidth = 0;

        AtomicInteger maxHeightElement = new AtomicInteger(0);

        ArrayList<UMLClass> drawn = new ArrayList<>();

        drawParents(umlClasses, drawn, coordinateX, coordinateY,maxHeightElement, g2d);

        coordinateY += maxHeightElement.get() + 100;
        maxWidth = Integer.max(coordinateX.get(), maxWidth);
        coordinateX.set(5);
        maxHeightElement.set(0);

        drawChildren(umlClasses, drawn, coordinateX, coordinateY, maxHeightElement, g2d);

        drawInners(umlClasses, coordinateX, coordinateY, maxHeightElement, g2d);

        coordinateY += maxHeightElement.get() + 100;
        maxHeightElement.set(0);
        maxWidth = Integer.max(coordinateX.get(), maxWidth);
        coordinateX.set(5);

        drawSingle(umlClasses, drawn, coordinateX, coordinateY, maxHeightElement, g2d);

        newSize.width = maxWidth;
        newSize.height = coordinateY + maxHeightElement.get();

        for (UMLClass umlClass : umlClasses) {
            for (int i = 0; i < umlClass.getChildren().size(); i++)
                umlClass.linkClass(umlClass.getChildren().get(i), umlClass.getChildren().get(i).getLinkClass(10, i+1, umlClass.getChildren().size()), g2d);
            for (int i = 0; i < umlClass.getInners().size(); i++)
                umlClass.linkClass(umlClass.getInners().get(i), umlClass.getInners().get(i).getLinkClass(10, i+1, umlClass.getInners().size()), g2d);
        }
    }

    private void drawParents(ArrayList<UMLClass> umlClasses, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY, AtomicInteger maxHeightElement, Graphics2D g2d) {
        for (UMLClass umlClass : umlClasses) {
            if (umlClass.getChildren().size() > 0) {
                umlClass.defX(coordinateX.get()).defY(coordinateY).draw(g2d);
                coordinateX.addAndGet((int) (umlClass.getWidth() + 100));
                maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) umlClass.getHeight()));
                drawn.add(umlClass);
            }
        }
    }

    private void drawChildren(ArrayList<UMLClass> umlClasses, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY, AtomicInteger maxHeightElement, Graphics2D g2d) {
        for (UMLClass umlClass : umlClasses) {
            if (umlClass.getParents().size() > 0) {
                if (!drawn.contains(umlClass)) {
                    umlClass.defX(coordinateX.get()).defY(coordinateY).draw(g2d);
                    coordinateX.addAndGet((int) (umlClass.getWidth() + 100));
                    maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) umlClass.getHeight()));
                    drawn.add(umlClass);
                }
            }
        }
    }

    private void drawInners(ArrayList<UMLClass> umlClasses, AtomicInteger coordinateX, int coordinateY, AtomicInteger maxHeightElement, Graphics2D g2d) {
        for (UMLClass umlClass : umlClasses) {
            if (umlClass.getInners().size() > 0) {
                for (UMLClass inner : umlClass.getInners()) {
                    inner.defX(coordinateX.get()).defY(coordinateY).draw(g2d);
                    coordinateX.addAndGet((int)(inner.getWidth() + 100));
                    maxHeightElement.set(Integer.max(maxHeightElement.get(), (int)inner.getHeight()));
                }
            }
        }
    }

    private void drawSingle(ArrayList<UMLClass> umlClasses, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY, AtomicInteger maxHeightElement, Graphics2D g2d) {
        for (UMLClass umlClass : umlClasses) {
            if (umlClass.getParents().size() == 0 &&
                    umlClass.getChildren().size() == 0) {
                umlClass.defX(coordinateX.get()).defY(coordinateY).draw(g2d);
                coordinateX.addAndGet((int) (umlClass.getWidth() + 100));
                maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) umlClass.getHeight()));
                drawn.add(umlClass);
            }
        }
    }

    private ArrayList<UMLClass> getUMLClassesFromPsiClasses(Object[] selectedClasses) {
        ArrayList<UMLClass> classes = new ArrayList<>();

        ArrayList<String> fields;
        ArrayList<String> methods;

        for (Object classUnit : selectedClasses) {
            if (classUnit.toString().contains("PsiClass")) {

                PsiReferenceList extendsList = ((PsiClass) classUnit).getExtendsList();
                PsiReferenceList implementsList = ((PsiClass) classUnit).getImplementsList();
                PsiClass[] innersList = ((PsiClass)classUnit).getInnerClasses();

                UMLClass added = new UMLClass();

                if (extendsList != null) added.defRawExtends(extendsList.getReferenceElements());
                if (implementsList != null) added.defRawImplements(implementsList.getReferenceElements());
                added.defRawInners(innersList);

                fields = psiFieldsToStringArray((PsiClass) classUnit);
                methods = psiMethodsToStringArray((PsiClass) classUnit);

                classes.add(added.defType(getInActionClassType((PsiClass) classUnit))
                        .defName(((PsiClass) classUnit).getName())
                        .defFields(fields)
                        .defMethods(methods)
                        .calculateSizeClassRectangle());
            }
        }

        return classes;
    }

    private void structureUmlClasses(ArrayList<UMLClass> umlClasses) {
        for (int i = 0; i < umlClasses.size(); i++) {
            defineParentsAsExtends(umlClasses, i);
            defineParentsAsImplements(umlClasses, i);
            defineInnerClasses(umlClasses, i);
        }
    }

    private void defineParentsAsExtends(ArrayList<UMLClass> umlClasses, int i) {
        boolean extendsInUmlClasses = false;
        if (umlClasses.get(i).getRawExtends() != null && umlClasses.get(i).getRawExtends().length > 0) {
            for (PsiJavaCodeReferenceElement element : umlClasses.get(i).getRawExtends()) {
                for (int j = 0; j < umlClasses.size(); j++) {
                    if (j != i && Objects.equals(element.getReferenceName(), umlClasses.get(j).getName())) {
                        umlClasses.get(i).addParent(umlClasses.get(j));
                        umlClasses.get(j).addChild(umlClasses.get(i));
                        umlClasses.get(i).linkType = UMLRelationship.LinkType.INHERIT;
                        extendsInUmlClasses = true;
                    }
                }
                if (!extendsInUmlClasses) defineUnknownClass(umlClasses, UMLClass.ClassType.CLASS, element, i);
            }
        }
    }

    private void defineParentsAsImplements(ArrayList<UMLClass> umlClasses, int i) {
        boolean implementsInUmlClasses = false;
        if (umlClasses.get(i).getRawImplements() != null && umlClasses.get(i).getRawImplements().length > 0) {
            for (PsiJavaCodeReferenceElement element : umlClasses.get(i).getRawImplements()) {
                for (int j = 0; j < umlClasses.size(); j++) {
                    if (j != i && Objects.equals(element.getReferenceName(), umlClasses.get(j).getName())) {
                        umlClasses.get(i).addParent(umlClasses.get(j));
                        umlClasses.get(j).addChild(umlClasses.get(i));
                        umlClasses.get(i).linkType = UMLRelationship.LinkType.DEPENDENCE;
                        implementsInUmlClasses = true;
                    }
                }
                if (!implementsInUmlClasses) defineUnknownClass(umlClasses, UMLClass.ClassType.INTERFACE, element, i);
            }
        }
    }
    private void defineInnerClasses(ArrayList<UMLClass> umlClasses, int i) {
        if (umlClasses.get(i).getRawInners() != null && umlClasses.get(i).getRawInners().length > 0) {
            for (PsiClass element : umlClasses.get(i).getRawInners()) {
                UMLClass added = new UMLClass();
                ArrayList<String> fields = psiFieldsToStringArray(element);
                ArrayList<String> methods = psiMethodsToStringArray(element);

                added.linkType = UMLRelationship.LinkType.INNER;

                umlClasses.get(i).addInner(added.defType(getInActionClassType(element))
                        .defName(element.getName())
                        .defFields(fields)
                        .defMethods(methods)
                        .calculateSizeClassRectangle());
            }
        }
    }

    private void defineUnknownClass(ArrayList<UMLClass> umlClasses, UMLClass.ClassType umlType, PsiJavaCodeReferenceElement element, int i) {
        UMLClass umlClass = new UMLClass()
                .defName(element.getReferenceName())
                .defType(umlType)
                .addChild(umlClasses.get(i));

        umlClass.linkType = UMLRelationship.LinkType.INHERIT;
        umlClasses.get(i).addParent(umlClass);

        umlClasses.add(umlClass);
    }

    private ArrayList<String> psiFieldsToStringArray(PsiClass classUnit) {
        ArrayList<String> fields = new ArrayList<>();
        for (PsiField field : classUnit.getFields()) {
            fields.add("-" + field.getName() + "  " + field.getType().toString().replace("PsiType", ""));
        }
        return fields;
    }

    private ArrayList<String> psiMethodsToStringArray(PsiClass classUnit) {
        ArrayList<String> methods = new ArrayList<>();
        for (PsiMethod method : classUnit.getMethods()) {
            methods.add("+" + method.getName() + "  " + (method.getReturnTypeElement() == null ? "constructor" : method.getReturnTypeElement().toString().replace("PsiTypeElement", "")));
        }
        return methods;
    }

    private Graphics2D createGraphics(BufferedImage bufferedImage, int width, int height) {
        Graphics2D g2d = bufferedImage.createGraphics();
        Color background = JBColor.white;

        g2d.setPaint(background);
        g2d.setColor(background);
        g2d.fillRect(0, 0, width, height);

        return g2d;
    }

    private UMLClass.ClassType getInActionClassType(PsiClass item) {
        return item.isInterface() ? UMLClass.ClassType.INTERFACE :
                Objects.requireNonNull(item.getModifierList()).toString().contains("abstract") ? UMLClass.ClassType.ABSTRACT :
                        item.isEnum() ? UMLClass.ClassType.ENUM: UMLClass.ClassType.CLASS;
    }

    private VirtualFile getChooserVirtualFile(AnActionEvent event) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        Project project = getEventProject(event);

        return FileChooser.chooseFile(descriptor, project, null);
    }

    private WindowSize calculateWindowSize(ArrayList<UMLClass> umlClasses) {
        WindowSize windowSize = new WindowSize();

        for (UMLClass umlClass : umlClasses) {
            windowSize.width += umlClass.getWidth() + 100;
            windowSize.height = Integer.max(windowSize.height, (int) umlClass.getHeight());
        }

        windowSize.height = windowSize.height*2 + 200;

        return windowSize;
    }

    private void saveGraphicsAsImage(BufferedImage bufferedImage, String path) {
        try {
            ImageIO.write(bufferedImage, "png", new File(path + "/image.png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
