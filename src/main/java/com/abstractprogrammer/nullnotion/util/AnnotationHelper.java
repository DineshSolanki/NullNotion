package com.abstractprogrammer.nullnotion.util;

import com.abstractprogrammer.nullnotion.model.DatabaseConnection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
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
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class AnnotationHelper {
    private final Logger logger = Logger.getInstance(getClass());
    private static final String COLUMN_ANNOTATION = "javax.persistence.Column";
    private static final String TABLE_ANNOTATION = "javax.persistence.Table";
    private static final String JOIN_COLUMN_ANNOTATION = "javax.persistence.JoinColumn";
    private static final String NULLABLE_IMPORT = "org.springframework.lang.Nullable";
    private static final String NON_NULL_IMPORT = "org.springframework.lang.NonNull";
    private Project project;

    public void processAnnotations(Project project, PsiJavaFile psiJavaFile, PsiClass selectedClass) {
        this.project = project;
        Optional<DatabaseConnection> connectionProperties = DatabaseHelper.getConnectionProperties(project);
        if (connectionProperties.isEmpty()) {
            Messages.showErrorDialog(project, "No database connection properties found", "Connection Error");
            return;
        }
        DatabaseConnection databaseConnection = connectionProperties.get();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Processing null notion annotations", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // set the total number of steps for the progress bar
                indicator.setIndeterminate(true);
                indicator.setFraction(0);
                indicator.setText("Scanning class");

                String entityClassName = ReadAction.compute(() -> getNameFromAnnotation(selectedClass.getAnnotation(TABLE_ANNOTATION)));
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                //establish a connection to the database and retrieve the schema
                try {
                    indicator.setFraction(0.2);
                    indicator.setText("Retrieving database schema");
                    Optional<Connection> connectionOptional = DatabaseHelper.getDatabaseDriver(project, databaseConnection);
                    indicator.checkCanceled();
                    if (connectionOptional.isEmpty()) {
                        ApplicationManager.getApplication().invokeAndWait(() ->
                                Messages.showErrorDialog(project,
                                        "Could not create connection",
                                        "Connection Error"));
                        return;
                    }
                    try (Connection connection = connectionOptional.get()) {
                        DatabaseMetaData metaData = connection.getMetaData();
                        indicator.setFraction(0.4);
                        indicator.setText(String.format("Processing class %s (%s)", selectedClass.getName(), entityClassName));
                        processTable(selectedClass, entityClassName, elementFactory, metaData);
                        indicator.setFraction(0.6);
                        indicator.setText("Importing annotations");
                        importAnnotations(psiJavaFile);
                        indicator.setFraction(0.8);
                        indicator.setText("Saving file");
                        saveDocument(psiJavaFile);
                    }
                } catch (IllegalArgumentException | SQLException ex) {
                    logger.error(ex);
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(project, ex.getMessage(), "Error"));
                }
            }
        });
    }

    /**
     * save the document if it is modified
     *
     * @param psiJavaFile the file to save
     */
    private void saveDocument(PsiJavaFile psiJavaFile) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(psiJavaFile);
        if (document != null && psiDocumentManager.isDocumentBlockedByPsi(document)) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                psiDocumentManager.commitDocument(document);
            });
        }
    }

    /**
     * process the table and add the annotations to the class
     *
     * @param selectedClass  the class to process
     * @param tableName      the name of the table
     * @param elementFactory the element factory
     * @param metaData       the database metadata
     * @throws SQLException if there is an error retrieving the database metadata
     */
    private void processTable(PsiClass selectedClass, String tableName,
                              PsiElementFactory elementFactory, @NotNull DatabaseMetaData metaData) throws SQLException {

        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, null)) {
            Map<String, String> columnInfo = new HashMap<>();
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String isNullable = resultSet.getString("IS_NULLABLE");
                columnInfo.put(columnName, isNullable);
            }
            ReadAction.run(() -> {
                List<PsiField> columnFields = Arrays.stream(selectedClass.getFields())
                        .filter(psiField ->
                                psiField.hasAnnotation(COLUMN_ANNOTATION) || psiField.hasAnnotation(JOIN_COLUMN_ANNOTATION))
                        .collect(Collectors.toList());
                for (PsiField field : columnFields) {
                    String fieldName = getFieldName(field);
                    if (columnInfo.containsKey(fieldName)) {
                        String isNullable = columnInfo.get(fieldName);
                        PsiAnnotation annotation = isNullable.equals("NO") ?
                                elementFactory.createAnnotationFromText("@NonNull", field)
                                : elementFactory.createAnnotationFromText("@Nullable", field);
                        addAnnotation(field, annotation);
                    }
                }
            });
        }
    }

    /**
     * gets the name of the field from the annotation
     *
     * @param field the field to get the name from
     * @return the name of the field
     */
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

    /**
     * Adds the annotation to the field if it does not already exist
     *
     * @param field      the field to add the annotation to
     * @param annotation the annotation to add
     */
    private void addAnnotation(@NotNull PsiField field, PsiAnnotation annotation) {
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null) {
                boolean existingAnnotation = modifierList.hasAnnotation(qualifiedName);
                if (!existingAnnotation) {
                    ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, (Computable<PsiElement>) () -> modifierList.addAfter(annotation, null)));
                }
            }
        }
    }

    /**
     * Extracts the name attribute from the annotation
     *
     * @param annotation the annotation to extract the name from
     * @return the name attribute value
     */
    private static String getNameFromAnnotation(PsiAnnotation annotation) {
        String stringValue = "";
        if (annotation != null) {
            PsiAnnotationMemberValue nameValuePair = annotation.findDeclaredAttributeValue("name");
            if (nameValuePair != null) {
                //check if it's a literal
                if (nameValuePair instanceof PsiLiteralExpression) {
                    Object value = ((PsiLiteralExpression) nameValuePair).getValue();
                    if (value != null) {
                        stringValue = value.toString();
                    }
                }
                //or reference to a constant
                else if (nameValuePair instanceof PsiReferenceExpression) {
                    PsiElement qualifier = ((PsiReferenceExpression) nameValuePair).getQualifier();
                    if (qualifier != null) {
                        stringValue = qualifier.getText();
                    }
                }
            }
        }
        return stringValue.replaceAll("\"", "");
    }

    /**
     * Imports the javax.annotation.Nullable and javax.annotation.Nonnull annotations
     *
     * @param psiJavaFile the file to import the annotations into
     */
    private void importAnnotations(@NotNull PsiJavaFile psiJavaFile) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiImportStatement nullableImport = ReadAction.compute(() -> 
                elementFactory.createImportStatement(findClass(NULLABLE_IMPORT)));
        PsiImportStatement nonNullImport = ReadAction.compute(() -> elementFactory.createImportStatement(findClass(NON_NULL_IMPORT)));
        PsiImportList importList = ReadAction.compute(psiJavaFile::getImportList);
        if (importList != null) {
            PsiElement lastImport = ReadAction.compute(importList::getLastChild);
            PsiImportStatement[] importStatements = ReadAction.compute(importList::getImportStatements);
            if (Arrays.stream(importStatements).noneMatch(importStatement ->
                    Objects.equals(ReadAction.compute(importStatement::getQualifiedName), NULLABLE_IMPORT))) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    importList.addAfter(nullableImport, lastImport);
                });
            }
            if (Arrays.stream(importStatements).noneMatch(importStatement ->
                    Objects.equals(ReadAction.compute(importStatement::getQualifiedName), NON_NULL_IMPORT))) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    importList.addAfter(nonNullImport, lastImport);
                });
            }
        }
    }

    /**
     * find a class by its fully qualified name
     *
     * @param className fully qualified name
     * @return the class or null if not found
     */
    private PsiClass findClass(String className) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        return ReadAction.compute(() -> JavaPsiFacade.getInstance(project).findClass(className, scope));
    }
}
