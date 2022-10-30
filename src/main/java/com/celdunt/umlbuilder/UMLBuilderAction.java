package com.celdunt.umlbuilder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final int widthWindow = 1000;
        final int heightWindow = 1000;

        final VirtualFile chooserVFile = getChooserVirtualFile(e);

        final BufferedImage bufferedImage = createImage(widthWindow, heightWindow, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = createGraphics(bufferedImage, JBColor.white, widthWindow, heightWindow);

        Object[] selectedClasses = e.getRequiredData(PlatformDataKeys.SELECTED_ITEMS);

        drawUML(selectedClasses, g2d);
        if (chooserVFile != null) saveGraphicsAsImage(bufferedImage, chooserVFile.getPath());
    }

    private void drawUML(Object[] selectedClasses, Graphics2D g2d) {
        ArrayList<String> fieldsArray = new ArrayList<>();
        ArrayList<String> methodsArray = new ArrayList<>();

        ArrayList<UMLClass> classes = new ArrayList<>();

        int coordX = 50;
        int coordY = 50;

        for (Object classUnit : selectedClasses) {
            System.out.println(classUnit.toString());
            if (classUnit.toString().contains("PsiClass")) {
                for (PsiField field : ((PsiClass) classUnit).getFields())
                    fieldsArray.add("-" + field.getName() + "  " + field.getType().toString().replace("PsiType", ""));

                for (PsiMethod method : ((PsiClass) classUnit).getMethods())
                    methodsArray.add("+" + method.getName() + "  " + Objects.requireNonNull(method.getReturnTypeElement()).toString().replace("PsiTypeElement", ""));

                System.out.println(fieldsArray.size());
                System.out.println(methodsArray.size());

                final String className = ((PsiClass) classUnit).getName();
                final String classType = getInActionClassType((PsiClass) classUnit);

                classes.add(new UMLClass()
                        .defX(coordX)
                        .defY(coordY)
                        .defType(classType)
                        .defName(className)
                        .defFields(fieldsArray)
                        .defMethods(methodsArray));

                coordX += 200;

                fieldsArray.clear();
                methodsArray.clear();
            }
        }

        for (UMLClass classItem : classes)
            classItem.draw(g2d);

        if (classes.size() > 1)
            classes.get(0).linkClass(classes.get(1), new UMLDependenceRelationship(), g2d);
    }
    private Graphics2D createGraphics(BufferedImage bufferedImage, Color background, int width, int height) {
        final Graphics2D g2d = bufferedImage.createGraphics();

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
        VirtualFile virtualFile = null;

        return FileChooser.chooseFile(descriptor, project, virtualFile);
    }
    private void saveGraphicsAsImage(BufferedImage bufferedImage, String path) {
        try {
            ImageIO.write(bufferedImage, "png", new File(path + "/image.png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
