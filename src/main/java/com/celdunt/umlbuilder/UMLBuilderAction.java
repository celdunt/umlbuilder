package com.celdunt.umlbuilder;

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

import static com.intellij.util.ui.ImageUtil.createImage;

public class UMLBuilderAction extends AnAction {

    /*
    *  1≥ определить кол-во выделенных классов
    *  2≥ определить размер окна, согласно пункту "1≥"
    *  3≥ отрисовать классы
    */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object[] selectedClasses = e.getRequiredData(PlatformDataKeys.SELECTED_ITEMS);

        ArrayList<UMLClass> umlClasses = getUMLClassesFromPsiClasses(selectedClasses);

        structureUmlClasses(umlClasses);

        int windowSize = umlClasses.size() * 500;

        BufferedImage bufferedImage = createImage(windowSize, windowSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = createGraphics(bufferedImage, windowSize, windowSize);

        // ≥ Нарисуем родителей
        // ≥ Нарисуем детей(тех что ещё не отрисованы на предыдущем пункте
        // ≥ Нарисуем стрело4ки

        drawUML(umlClasses, g2d);

        VirtualFile chooserVFile = getChooserVirtualFile(e);

        if (chooserVFile != null) saveGraphicsAsImage(bufferedImage, chooserVFile.getPath());
    }

    private void drawUML(ArrayList<UMLClass> umlClasses, Graphics2D g2d) {
        int coordinateX = 50;
        int coordinateY = 50;

        for (UMLClass umlClass : umlClasses) {
            umlClass.defX(coordinateX).defY(coordinateY).draw(g2d);
            coordinateX += 400;
        }

        if (umlClasses.size() > 1)
            umlClasses.get(0).linkClass(umlClasses.get(1), new UMLInheritRelationship(10, 1, 1), g2d);

        //Добавить расчёт координат и свзяей!!!! ----> СРОЧНО!!!
    }
    private ArrayList<UMLClass> getUMLClassesFromPsiClasses(Object[] selectedClasses) {
        ArrayList<UMLClass> classes = new ArrayList<>();

        ArrayList<String> fields;
        ArrayList<String> methods;

        for (Object classUnit : selectedClasses) {
            if (classUnit.toString().contains("PsiClass")) {

                PsiReferenceList extendsList = ((PsiClass)classUnit).getExtendsList();
                PsiReferenceList implementsList = ((PsiClass)classUnit).getImplementsList();

                UMLClass added = new UMLClass();

                if (extendsList != null) added.defRawExtends(extendsList.getReferenceElements());
                if (implementsList != null) added.defRawImplements(implementsList.getReferenceElements());

                fields = psiFieldsToStringArray((PsiClass) classUnit);
                methods = psiMethodsToStringArray((PsiClass) classUnit);

                classes.add(added.defType(getInActionClassType((PsiClass) classUnit))
                        .defName(((PsiClass) classUnit).getName())
                        .defFields(fields)
                        .defMethods(methods));
            }
        }

        return classes;
    }
    private void structureUmlClasses(ArrayList<UMLClass> umlClasses) {
        for (int i = 0; i < umlClasses.size()-1; i++) {
            defineParentsAsExtends(umlClasses, i);
            defineParentsAsImplements(umlClasses, i);
        }
    }
    private void defineParentsAsExtends(ArrayList<UMLClass> umlClasses, int i) {
        if (umlClasses.get(i).getRawExtends().length > 0) {
            for (PsiJavaCodeReferenceElement element : umlClasses.get(i).getRawExtends()) {
                for (int j = i+1; j < umlClasses.size(); j++) {
                    if (Objects.equals(element.getReferenceName(), umlClasses.get(j).getName())) {
                        umlClasses.get(i).addParent(umlClasses.get(j));
                        umlClasses.get(j).addChild(umlClasses.get(i));
                        umlClasses.get(j).linkType = UMLRelationship.LinkType.INHERIT;
                    }
                }
            }
        }
    }
    private void defineParentsAsImplements(ArrayList<UMLClass> umlClasses, int i) {
        if (umlClasses.get(i).getRawImplements().length > 0) {
            for (PsiJavaCodeReferenceElement element : umlClasses.get(i).getRawImplements()) {
                for (int j = i+1; j < umlClasses.size(); j++) {
                    if (Objects.equals(element.getReferenceName(), umlClasses.get(j).getName())) {
                        umlClasses.get(i).addParent(umlClasses.get(j));
                        umlClasses.get(j).addChild(umlClasses.get(i));
                        umlClasses.get(j).linkType = UMLRelationship.LinkType.DEPENDENCE;
                    }
                }
            }
        }
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
            methods.add("+" + method.getName() + "  " + (method.getReturnTypeElement() == null? "constructor": method.getReturnTypeElement().toString().replace("PsiTypeElement", "")));
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
    private String getInActionClassType(PsiClass item) {
        return item.isInterface()? UMLClass.interfaceType:
                Objects.requireNonNull(item.getModifierList()).toString().contains("abstract")? UMLClass.abstractType:
                        UMLClass.classType;
    }
    private VirtualFile getChooserVirtualFile(AnActionEvent event) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        Project project = getEventProject(event);

        return FileChooser.chooseFile(descriptor, project, null);
    }
    private void saveGraphicsAsImage(BufferedImage bufferedImage, String path) {
        try {
            ImageIO.write(bufferedImage, "png", new File(path + "/image.png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
