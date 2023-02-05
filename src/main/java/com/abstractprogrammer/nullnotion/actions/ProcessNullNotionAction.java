package com.abstractprogrammer.nullnotion.actions;

import com.abstractprogrammer.nullnotion.util.AnnotationHelper;
import com.abstractprogrammer.nullnotion.util.CommonUtil;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessNullNotionAction extends AnAction {
    private static final String ENTITY_ANNOTATION = "javax.persistence.Entity";
    private final Logger logger = Logger.getInstance(getClass());
    final AnnotationHelper annotationHelper = new AnnotationHelper();

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        try {
            logger.info("Update method called");
            Project project = e.getProject();
            VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            presentation.setEnabledAndVisible(shouldEnableAction(project, selectedFiles));
        } catch (IndexNotReadyException ex) {
            logger.warn("Index is not ready, disable action");
            presentation.setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //get the project
        Project project = e.getProject();
        if (project != null) {
            VirtualFile[] virtualFiles = ReadAction.compute(() -> e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
            int processedFiles = 0;
            for (VirtualFile virtualFile : virtualFiles) {
                PsiJavaFile psiJavaFile = ReadAction.compute(() -> 
                        (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile));
                if (psiJavaFile != null) {
                    List<PsiClass> classes = ReadAction.compute(() ->
                            Arrays.stream(psiJavaFile.getClasses())
                                    .filter(psiClass -> psiClass.hasModifier(JvmModifier.PUBLIC))
                                    .collect(Collectors.toList()));
                    if (!classes.isEmpty()) {
                        PsiClass firstClass = classes.get(0); //as only one class can has public modifier
                        if (firstClass != null) {
                            Boolean isEntity = ReadAction.compute(() -> firstClass.hasAnnotation(ENTITY_ANNOTATION));
                            if (isEntity) {
                                annotationHelper.processAnnotations(project, psiJavaFile, firstClass);
                                processedFiles++;
                            }
                        }
                    }
                }
                CommonUtil.showBalloonNotification(project,
                        String.format("Processed %d files", processedFiles),
                        MessageType.INFO);
            }
        }
    }

    /**
     * Determines if the action should be enabled based on the given project and selected file.
     * also sets the selectedClass and psiJavaFile fields
     *
     * @param project      the current project
     * @param selectedFiles the currently selected file
     * @return true if the action should be enabled, false otherwise
     */
    private boolean shouldEnableAction(Project project, VirtualFile @Nullable [] selectedFiles) {
        boolean shouldEnable = false;
        if (project == null) {
            logger.warn("Project is null");
        } else if (selectedFiles == null || selectedFiles.length == 0) {
            logger.warn("Selected file is null");
        } else if (!DumbService.isDumb(project)) {
            for (VirtualFile file : selectedFiles) {
                if (file == null || !file.getName().endsWith(".java")) {
                    logger.warn("Selected file is not a java file");
                    continue;
                }
                PsiJavaFile psiJavaFile = ReadAction.compute(() -> (PsiJavaFile) PsiManager.getInstance(project).findFile(file));
                if (psiJavaFile == null) {
                    logger.warn("could not find psi file");
                } else {
                    List<PsiClass> classes = ReadAction.compute(() -> 
                            Arrays.stream(psiJavaFile.getClasses())
                                    .filter(psiClass -> psiClass.hasModifier(JvmModifier.PUBLIC))
                                    .collect(Collectors.toList()));
                    if (!classes.isEmpty()) {
                        PsiClass firstClass = classes.get(0); //as only one class can has public modifier
                        if (firstClass != null) {
                            Boolean isEntity = ReadAction.compute(() -> firstClass.hasAnnotation(ENTITY_ANNOTATION));
                            if (isEntity) {
                                shouldEnable = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return shouldEnable;
    }
}

