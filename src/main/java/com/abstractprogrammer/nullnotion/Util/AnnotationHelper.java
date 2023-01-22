package com.abstractprogrammer.nullnotion.Util;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


public class AnnotationHelper {
    private final Logger logger = Logger.getInstance(getClass());
    private static final String COLUMN_ANNOTATION = "javax.persistence.Column";
    private static final String JOIN_COLUMN_ANNOTATION = "javax.persistence.JoinColumn";
    public static final String NULLABLE_IMPORT = "org.springframework.lang.Nullable";
    public static final String NON_NULL_IMPORT = "org.springframework.lang.NonNull";

    public void processAnnotations(Project project, PsiJavaFile psiJavaFile, PsiClass selectedClass) {
        String entityClassName = selectedClass.getName();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        //establish a connection to the database and retrieve the schema
        try {
            Optional<Connection> connectionOptional = DatabaseHelper.getDatabaseDriver(project);
            if (connectionOptional.isEmpty()) {
                Messages.showErrorDialog(project, "Could not create connection", "Connection Error");
                return;
            }
            try (Connection connection = connectionOptional.get()) {
                DatabaseMetaData metaData = connection.getMetaData();
                processTable(project, selectedClass, entityClassName, elementFactory, metaData);
                importAnnotations(project, psiJavaFile);
                saveDocument(project, psiJavaFile);
            }
            Messages.showInfoMessage(project, "Null Notion processing complete", "Success");
        } catch (IllegalArgumentException | SQLException | ClassNotFoundException ex) {
            logger.error(ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error");
        }
    }

    private static void saveDocument(Project project, PsiJavaFile psiJavaFile) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(psiJavaFile);
        if (document != null && psiDocumentManager.isDocumentBlockedByPsi(document)) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                psiDocumentManager.commitDocument(document);
            });
        }
    }
    
    private void processTable(Project project, PsiClass selectedClass, String tableName,
                              PsiElementFactory elementFactory, @NotNull DatabaseMetaData metaData) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
            if (resultSet.next()) {
                ReadAction.run(() -> {
                    for (PsiField field : selectedClass.getFields()) {
                        String fieldName = getFieldName(field);
                        try (ResultSet columnInfo = metaData.getColumns(null, null, tableName, fieldName)) {
                            if (columnInfo.next()) {
                                String isNullable = columnInfo.getString("IS_NULLABLE");
                                PsiAnnotation annotation = isNullable.equals("NO") ? 
                                        elementFactory.createAnnotationFromText("@NonNull", field) 
                                        : elementFactory.createAnnotationFromText("@Nullable", field);
                                addAnnotation(project, field, annotation);
                            }
                        }
                    }
                });
            }
        }
    }

    private static String getFieldName(@NotNull PsiField field) {
        String fieldName = field.getName();
        PsiAnnotation fieldAnnotation = field.getAnnotation(COLUMN_ANNOTATION);
        if (fieldAnnotation != null) {
            fieldName = getNameFromAnnotation(fieldAnnotation);
        } else {
            fieldAnnotation = field.getAnnotation(JOIN_COLUMN_ANNOTATION);
            if (fieldAnnotation != null) {
                fieldName = getNameFromAnnotation(fieldAnnotation);
            }
        }
        return fieldName;
    }

    private static void addAnnotation(Project project, @NotNull PsiField field, PsiAnnotation annotation) {
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null) {
                boolean existingAnnotation = modifierList.hasAnnotation(qualifiedName);
                if (!existingAnnotation) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        modifierList.addAfter(annotation, null);
                    });
                }
            }
        }
    }


    private static String getNameFromAnnotation(PsiAnnotation annotation) {
        String stringValue = "";
        if (annotation != null) {
            PsiAnnotationMemberValue nameValuePair = annotation.findDeclaredAttributeValue("name");
            if (nameValuePair != null) {
                if (nameValuePair instanceof PsiLiteralExpression) {
                    Object value = ((PsiLiteralExpression) nameValuePair).getValue();
                    if (value != null) {
                        stringValue = value.toString().replaceAll("\"", "");
                    }
                }
            }
        }
        return stringValue;
    }

    private void importAnnotations(Project project, @NotNull PsiJavaFile psiJavaFile) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiImportStatement nullableImport = elementFactory.createImportStatement(findClass(NULLABLE_IMPORT, project));
        PsiImportStatement nonNullImport = elementFactory.createImportStatement(findClass(NON_NULL_IMPORT, project));
        PsiImportList importList = psiJavaFile.getImportList();
        if (importList != null) {
            PsiElement lastImport = importList.getLastChild();
            if (Arrays.stream(importList.getImportStatements()).noneMatch(importStatement ->
                    Objects.equals(importStatement.getQualifiedName(), NULLABLE_IMPORT))) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    importList.addAfter(nullableImport, lastImport);
                });
            }
            if (Arrays.stream(importList.getImportStatements()).noneMatch(importStatement ->
                    Objects.equals(importStatement.getQualifiedName(), NON_NULL_IMPORT))) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    importList.addAfter(nonNullImport, lastImport);
                });
            }
        }
    }

    private PsiClass findClass(String className, Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        return JavaPsiFacade.getInstance(project).findClass(className, scope);
    }
}
