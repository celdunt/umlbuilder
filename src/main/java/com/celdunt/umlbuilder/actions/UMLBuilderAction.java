package com.celdunt.umlbuilder.actions;

import com.celdunt.umlbuilder.figures.UMLClass;
import com.celdunt.umlbuilder.relationships.UMLCompositionRelationship;
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
import java.util.regex.Pattern;

import static com.intellij.util.ui.ImageUtil.createImage;

public class UMLBuilderAction extends AnAction {
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

    /**
     * @param umlClasses лист классов, которые были получены из проекта
     * @param g2d        объект графики
     * @param newSize    размер окна(полотна)
     */
    private void drawUML(ArrayList<UMLClass> umlClasses, Graphics2D g2d, WindowSize newSize) {
        AtomicInteger coordinateX = new AtomicInteger(5);
        int coordinateY = 5;
        int maxWidth = 0;

        AtomicInteger maxHeightElement = new AtomicInteger(0);

        ArrayList<UMLClass> drawn = new ArrayList<>();

        defineParentsData(umlClasses, drawn, coordinateX, coordinateY, maxHeightElement);

        coordinateY += maxHeightElement.get() + 100;
        maxWidth = Integer.max(coordinateX.get(), maxWidth);
        coordinateX.set(5);
        maxHeightElement.set(0);

        defineOtherData(umlClasses, drawn, coordinateX, coordinateY, maxHeightElement);

        maxWidth = Integer.max(coordinateX.get(), maxWidth);

        newSize.width = maxWidth;
        newSize.height = maxHeightElement.get() + 10;

        drawRelations(umlClasses, g2d);
        drawClasses(drawn, g2d);
    }

    /**
     * @param umlClasses       Лист классов, которые будут отрисованы
     * @param drawn            Лист классов для заполнения; в этот лист будут добавлены уже отрисованные классы
     * @param coordinateX      переменная координаты икс, которая будет увеличиваться для каждого нового не отрисованного класса
     * @param coordinateY      переменная координаты игрек
     * @param maxHeightElement максимальная высота отрисованной фигуры; необходима для указания правильного отступа по вертикали
     */
    private void defineParentsData(ArrayList<UMLClass> umlClasses, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY,
                                   AtomicInteger maxHeightElement) {
        for (UMLClass umlClass : umlClasses) {
            if (umlClass.getChildren().size() > 0) {
                umlClass.defX(coordinateX.get()).defY(coordinateY);
                coordinateX.addAndGet((int) (umlClass.getWidth() + 100));
                maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) umlClass.getHeight()));
                drawn.add(umlClass);
            }
        }
    }

    /**
     * @param umlClasses       лист классов, которые будут отрисованы
     * @param drawn            лист уже отрисованных классов
     * @param coordinateX      координата икс
     * @param coordinateY      координата игрек
     * @param maxHeightElement максимальная высота отрисованного элемента
     */
    private void defineOtherData(ArrayList<UMLClass> umlClasses, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY,
                                 AtomicInteger maxHeightElement) {
        ArrayList<UMLClass> drawnInners = new ArrayList<>();
        AtomicInteger innerCoordinateX = new AtomicInteger(5);
        int innerCoordinateY = coordinateY + maxHeightElement.get();

        ArrayList<UMLClass> forAddition = new ArrayList<>(drawn);

        for (UMLClass umlClass : drawn) {
            for (UMLClass children : umlClass.getChildren())
                if (!forAddition.contains(children)) {
                    children.defX(coordinateX.get()).defY(coordinateY);
                    coordinateX.addAndGet((int) (children.getWidth() + 100));
                    maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) children.getHeight()));
                    forAddition.add(children);
                    innerCoordinateY = coordinateY + maxHeightElement.get() + 200;
                    innerCoordinateX.set(children.getX());
                    defineInnersData(children.getInners(), drawnInners, innerCoordinateX, innerCoordinateY, maxHeightElement);
                }
            innerCoordinateX.set(umlClass.getX());
            defineInnersData(umlClass.getInners(), drawnInners, coordinateX, coordinateY, maxHeightElement);
        }
        drawn.clear();
        drawn.addAll(forAddition);
        drawn.addAll(drawnInners);
        maxHeightElement.set(0);
        defineSingleData(umlClasses, drawn, innerCoordinateX, innerCoordinateY, maxHeightElement);
        maxHeightElement.addAndGet((int) (innerCoordinateY * 1.3));
    }

    /**
     * @param umlInners        лист классов, для отрисовки
     * @param drawn            лист уже отрисованных классов
     * @param coordinateX      координата икс
     * @param coordinateY      координата игрек
     * @param maxHeightElement максимальная высота уже отрисованного элемента
     */
    private void defineInnersData(ArrayList<UMLClass> umlInners, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY,
                                  AtomicInteger maxHeightElement) {
        ArrayList<UMLClass> forAddition = new ArrayList<>();
        for (UMLClass inner : umlInners)
            if (!drawn.contains(inner)) {
                inner.defX(coordinateX.get()).defY(coordinateY);
                coordinateX.addAndGet((int) (inner.getWidth() + 100));
                maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) inner.getHeight()));
                forAddition.add(inner);
            }
        drawn.addAll(forAddition);
    }

    /**
     * @param umlClasses       лист классов для отрисовки
     * @param coordinateX      координата икс
     * @param coordinateY      координата игрек
     * @param maxHeightElement максимальная высота отрисованного элемента
     */
    private void defineSingleData(ArrayList<UMLClass> umlClasses, ArrayList<UMLClass> drawn, AtomicInteger coordinateX, int coordinateY, AtomicInteger maxHeightElement) {
        for (UMLClass umlClass : umlClasses) {
            if (umlClass.getParents().size() == 0 &&
                    umlClass.getChildren().size() == 0) {
                umlClass.defX(coordinateX.get()).defY(coordinateY);
                coordinateX.addAndGet((int) (umlClass.getWidth() + 100));
                maxHeightElement.set(Integer.max(maxHeightElement.get(), (int) umlClass.getHeight()));
                drawn.add(umlClass);
            }
        }
    }

    /**
     * @param umlClasses лист отрисованных классов
     * @param g2d        объект графики
     */
    private void drawRelations(ArrayList<UMLClass> umlClasses, Graphics2D g2d) {
        for (UMLClass umlClass : umlClasses) {
            int numerator = 1;
            int denominator = umlClass.getChildren().size() + umlClass.getInners().size() + umlClass.getCompositions().size();

            for (UMLClass children : umlClass.getChildren())
                umlClass.linkClass(children, children.getLinkClass(10, numerator++, denominator), g2d);
            for (UMLClass inner : umlClass.getInners())
                umlClass.linkClass(inner, inner.getLinkClass(10, numerator++, denominator), g2d);
            for (UMLClass composite : umlClass.getCompositions())
                umlClass.linkClass(composite, new UMLCompositionRelationship(10, numerator++, denominator), g2d);
        }
    }

    private void drawClasses(ArrayList<UMLClass> umlClasses, Graphics2D g2d) {
        for (UMLClass umlClass : umlClasses)
            umlClass.draw(g2d);
    }

    /**
     * @param selectedClasses массив объектов выделенных в проекте, где необходимо построить диаграмму классов
     * @return возвращает лист UMLClass, в котором указаны все параметры классов
     */
    private ArrayList<UMLClass> getUMLClassesFromPsiClasses(Object[] selectedClasses) {
        ArrayList<UMLClass> classes = new ArrayList<>();

        ArrayList<String> fields;
        ArrayList<String> methods;

        for (Object classUnit : selectedClasses) {
            if (classUnit.toString().contains("PsiClass")) {

                PsiReferenceList extendsList = ((PsiClass) classUnit).getExtendsList();
                PsiReferenceList implementsList = ((PsiClass) classUnit).getImplementsList();
                PsiClass[] innersList = ((PsiClass) classUnit).getInnerClasses();

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
                        .defClassCode(((PsiClass) classUnit).getText())
                        .calculateSizeClassRectangle());
            }
        }

        return classes;
    }

    /**
     * @param umlClasses лист классов, которые необходимо структурировать
     */
    private void structureUmlClasses(ArrayList<UMLClass> umlClasses) {
        for (int i = 0; i < umlClasses.size(); i++) {
            defineParentsAsExtends(umlClasses, i);
            defineParentsAsImplements(umlClasses, i);
            defineInnerClasses(umlClasses, i);
            defineCompositeClasses(umlClasses, i);
        }
    }

    /**
     * @param umlClasses лист классов
     * @param i          индекс объекта в листе классов
     */
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

    private void defineCompositeClasses(ArrayList<UMLClass> umlClasses, int i) {
        for (int j = 0; j < umlClasses.size(); j++) {
            Pattern pattern = Pattern.compile("\\W" + umlClasses.get(j).getName() + "\\W");

            if (j != i && pattern.matcher(umlClasses.get(i).getClassCode()).find()
                    && !umlClasses.get(i).getInners().contains(umlClasses.get(j))
                    && !umlClasses.get(i).getParents().contains(umlClasses.get(j))) {
                umlClasses.get(i).addComposite(umlClasses.get(j));
            }
        }
    }

    /**
     * @param umlClasses лист классов
     * @param umlType    тип класса
     * @param element    экземпляр объекта, указывающий на класс-родитель
     * @param i          индекс объекта в листе umlCLasses
     */
    private void defineUnknownClass(ArrayList<UMLClass> umlClasses, UMLClass.ClassType umlType, PsiJavaCodeReferenceElement element, int i) {
        UMLClass umlClass = new UMLClass()
                .defName(element.getReferenceName())
                .defType(umlType)
                .addChild(umlClasses.get(i));

        umlClass.linkType = UMLRelationship.LinkType.INHERIT;
        umlClasses.get(i).linkType = UMLRelationship.LinkType.INHERIT;
        umlClasses.get(i).addParent(umlClass);

        umlClasses.add(umlClass);
    }

    /**
     * @param classUnit объект класса, из которого необходимо "достать" лист полей
     * @return возврат строкового листа полей класса
     */
    private ArrayList<String> psiFieldsToStringArray(PsiClass classUnit) {
        ArrayList<String> fields = new ArrayList<>();
        for (PsiField field : classUnit.getFields()) {
            fields.add("-" + field.getName() + "  " + field.getType().toString().replace("PsiType", ""));
        }
        return fields;
    }

    /**
     * @param classUnit объект класса, из которого необходимо "достать" лист методов
     * @return возврат строкового листа методов класса
     */
    private ArrayList<String> psiMethodsToStringArray(PsiClass classUnit) {
        ArrayList<String> methods = new ArrayList<>();
        for (PsiMethod method : classUnit.getMethods()) {
            methods.add("+" + method.getName() + "  " + (method.getReturnTypeElement() == null ? "constructor" : method.getReturnTypeElement().toString().replace("PsiTypeElement", "")));
        }
        return methods;
    }

    /**
     * @param bufferedImage объект изображения
     * @param width         ширина изображения
     * @param height        высота изображения
     * @return возвращает объект графики
     */
    private Graphics2D createGraphics(BufferedImage bufferedImage, int width, int height) {
        Graphics2D g2d = bufferedImage.createGraphics();
        Color background = JBColor.white;

        g2d.setPaint(background);
        g2d.setColor(background);
        g2d.fillRect(0, 0, width, height);

        return g2d;
    }

    /**
     * @param item объект класса, тип которого необходимо определить
     * @return возвращает тип класса
     */
    private UMLClass.ClassType getInActionClassType(PsiClass item) {
        return item.isInterface() ? UMLClass.ClassType.INTERFACE :
                Objects.requireNonNull(item.getModifierList()).toString().contains("abstract") ? UMLClass.ClassType.ABSTRACT :
                        item.isEnum() ? UMLClass.ClassType.ENUM : UMLClass.ClassType.CLASS;
    }

    /**
     * @param event объект события, из него получаем объект проекта
     * @return возвращаем путь для сохранения изображения
     */
    private VirtualFile getChooserVirtualFile(AnActionEvent event) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        Project project = getEventProject(event);

        return FileChooser.chooseFile(descriptor, project, null);
    }

    /**
     * @param umlClasses лист классов
     * @return объект WindowSize -- размер окна
     */
    private WindowSize calculateWindowSize(ArrayList<UMLClass> umlClasses) {
        WindowSize windowSize = new WindowSize();

        for (UMLClass umlClass : umlClasses) {
            windowSize.width += umlClass.getWidth() + 100;
            windowSize.height = Integer.max(windowSize.height, (int) umlClass.getHeight());
        }

        windowSize.height = windowSize.height * 10 + 200;

        return windowSize;
    }

    /**
     * @param bufferedImage изображение для сохранения
     * @param path          путь, куда изображение будет сохранено
     */
    private void saveGraphicsAsImage(BufferedImage bufferedImage, String path) {
        try {
            ImageIO.write(bufferedImage, "png", new File(path + "/image.png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
