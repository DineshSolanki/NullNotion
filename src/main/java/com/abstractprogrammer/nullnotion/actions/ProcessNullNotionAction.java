package com.abstractprogrammer.nullnotion.actions;

import com.abstractprogrammer.nullnotion.util.AnnotationHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class ProcessNullNotionAction extends AnAction {
    private static final String ENTITY_ANNOTATION = "javax.persistence.Entity";
    private final Logger logger = Logger.getInstance(getClass());
    PsiClass selectedClass;
    PsiJavaFile psiJavaFile;
    final AnnotationHelper annotationHelper = new AnnotationHelper();

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        try {
            logger.info("Update method called");
            Project project = e.getProject();
            VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            presentation.setEnabledAndVisible(shouldEnableAction(project, selectedFile));
        } catch (IndexNotReadyException ex) {
            logger.warn("Index is not ready, disable action");
            presentation.setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //get the project
        Project project = e.getProject();
        if (selectedClass != null && psiJavaFile != null) {
            annotationHelper.processAnnotations(project, psiJavaFile, selectedClass);
        } else {
            Messages.showErrorDialog("Could not find the class", "Error");
        }
    }

    /**
     * Determines if the action should be enabled based on the given project and selected file.
     * also sets the selectedClass and psiJavaFile fields
     *
     * @param project      the current project
     * @param selectedFile the currently selected file
     * @return true if the action should be enabled, false otherwise
     */
    private boolean shouldEnableAction(Project project, VirtualFile selectedFile) {
        boolean isEnable = true;
        if (project == null) {
            logger.warn("Project is null");
            isEnable = false;
        } else if (selectedFile == null || !selectedFile.getName().endsWith(".java")) {
            logger.warn("Selected file is not a java file");
            isEnable = false;
        } else if (!DumbService.isDumb(project)) {
            psiJavaFile = ReadAction.compute(() -> (PsiJavaFile) PsiManager.getInstance(project).findFile(selectedFile));
            if (psiJavaFile == null) {
                logger.warn("could not find psi file");
                isEnable = false;
            } else {
                PsiClass[] classes = ReadAction.compute(() -> psiJavaFile.getClasses());
                if (classes.length > 0) {
                    selectedClass = classes[0];
                }
                if (selectedClass == null) {
                    logger.warn("could not find any class in the file");
                    isEnable = false;
                } else {
                    isEnable = ReadAction.compute(() -> selectedClass.hasAnnotation(ENTITY_ANNOTATION));
                }
            }
        }
        return isEnable;
    }
}

