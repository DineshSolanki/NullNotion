package com.abstractprogrammer.nullnotion.actions;

import com.abstractprogrammer.nullnotion.Util.AnnotationHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

public class ProcessNullNotionAction extends AnAction {
    private static final String ENTITY_ANNOTATION = "javax.persistence.Entity";
    private final Logger logger = Logger.getInstance(getClass());
    PsiClass selectedClass;
    PsiJavaFile psiJavaFile;
    AnnotationHelper annotationHelper = new AnnotationHelper();

    @Override
    public void update(@NotNull AnActionEvent e) {
        Application application = ApplicationManager.getApplication();
        try {
            application.executeOnPooledThread(() -> {
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
                    if (!DumbService.isDumb(project)) {
                        ReadAction.run(() ->{
                            psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(selectedFile);
                        });
                        if (psiJavaFile == null) {
                            logger.warn("could not find psi file");
                            presentation.setEnabledAndVisible(false);
                        } else {
                            ReadAction.run(() ->{
                                PsiClass[] classes = psiJavaFile.getClasses();
                                if (classes.length > 0) {
                                    selectedClass = classes[0];
                                }
                            });
                            if (selectedClass == null) {
                                logger.warn("could not find any class in the file");
                                presentation.setEnabledAndVisible(false);
                            } else {
                                ReadAction.run(() -> {
                                    PsiAnnotation entityAnnotation = selectedClass.getAnnotation(ENTITY_ANNOTATION);
                                    presentation.setEnabledAndVisible(entityAnnotation != null);
                                });
                            }
                        }
                    }
                }
            });
        } catch (IndexNotReadyException ex) {
            logger.warn("Index is not ready, disable action");
            e.getPresentation().setEnabledAndVisible(false);
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
}

