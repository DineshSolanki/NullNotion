package com.abstractprogrammer.nullnotion.actions;

import com.abstractprogrammer.nullnotion.Util.AnnotationHelper;
import com.abstractprogrammer.nullnotion.component.ConnectionSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

public class ProcessNullNotionAction extends AnAction {
    private final Logger logger = Logger.getInstance(getClass());
    private static final String ENTITY_ANNOTATION = "javax.persistence.Entity";
    PsiClass selectedClass;
    AnnotationHelper annotationHelper = new AnnotationHelper();

    @Override
    public void update(@NotNull AnActionEvent e) {
        logger.info("Update method called");
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null) {
            logger.warn("Project is null");
            presentation.setEnabledAndVisible(false);
        } else if (selectedFile == null || !selectedFile.getName().endsWith(".java")) {
            logger.warn("Selected file is not a java file");
            presentation.setEnabledAndVisible(false);
        } else {
            PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(selectedFile);
            if (psiJavaFile == null) {
                logger.warn("could not find psi file");
                presentation.setEnabledAndVisible(false);
            } else {
                selectedClass = psiJavaFile.getClasses()[0];
                if (selectedClass == null) {
                    logger.warn("could not find any class in the file");
                    presentation.setEnabledAndVisible(false);
                } else {
                    presentation.setEnabledAndVisible(selectedClass.getAnnotation(ENTITY_ANNOTATION) != null);
                }
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //get the project
        Project project = e.getProject();

        //get the editor
        PsiJavaFile psiJavaFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);
        //get the selected class
        if (selectedClass == null) {
            selectedClass = PsiTreeUtil.getParentOfType(psiJavaFile.findElementAt(e.getData(CommonDataKeys.CARET).getOffset()), PsiClass.class);
        }
        PsiAnnotation entityAnnotation = selectedClass.getAnnotation(ENTITY_ANNOTATION);
        if (entityAnnotation == null) {
            Messages.showErrorDialog(project, "Selected class is not an entity class", "Error");
            return;
        }
        //get the class name
        annotationHelper.processAnnotations(project, psiJavaFile, selectedClass);
    }
}

