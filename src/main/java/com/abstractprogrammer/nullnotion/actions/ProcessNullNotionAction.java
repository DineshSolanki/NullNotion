package com.abstractprogrammer.nullnotion.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Arrays;

public class ProcessNullNotionAction extends AnAction {
    private final Logger logger = Logger.getInstance(getClass());

    @Override
    public void actionPerformed(AnActionEvent e) {
        //get the project
        Project project = e.getProject();

        //get the editor
        PsiJavaFile psiJavaFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);

        //get the selected class
        PsiClass selectedClass = PsiTreeUtil.getParentOfType(psiJavaFile.findElementAt(e.getData(CommonDataKeys.CARET).getOffset()), PsiClass.class);
        PsiAnnotation entityAnnotation = selectedClass.getAnnotation("javax.persistence.Entity");
        if (entityAnnotation == null) {
            Messages.showErrorDialog(project, "Selected class is not an entity class", "Error");
            return;
        }
        //get the class name
        String entityClassName = selectedClass.getName();

        //prompt the user for the connection string
        String connectionString = Messages.showInputDialog(project, "Please enter the connection string for the database:", "Connection String", Messages.getQuestionIcon());
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        //establish a connection to the database and retrieve the schema
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(connectionString);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, entityClassName, null);
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                //iterate through fields of the class
                for (PsiField field : selectedClass.getFields()) {
                    String fieldName = field.getName();
                    PsiAnnotation fieldAnnotation = field.getAnnotation("javax.persistence.Column");
                    if (fieldAnnotation != null) {
                        fieldName = fieldAnnotation.findAttributeValue("name").getText().replaceAll("\"", "");
                    } else {
                        fieldAnnotation = field.getAnnotation("javax.persistence.JoinColumn");
                        if (fieldAnnotation != null) {
                            fieldName = fieldAnnotation.findAttributeValue("name").getText().replaceAll("\"", "");
                        }
                    }
                    ResultSet columnInfo = metaData.getColumns(null, null, tableName, fieldName);
                    if (columnInfo.next()) {
                        String isNullable = columnInfo.getString("IS_NULLABLE");
                        PsiAnnotation annotation;
                        if (isNullable.equals("NO")) {
                            annotation = elementFactory.createAnnotationFromText("@NonNull", field);
                        } else {
                            annotation = elementFactory.createAnnotationFromText("@Nullable", field);
                        }
                        // add the annotation to the field
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            field.getModifierList().addAfter(annotation, null);
                        });
                    }
                }
            }
            // Import the "org.springframework.lang.Nullable" annotation
            PsiImportStatement nullableImport = elementFactory.createImportStatement(psiFacade.findClass("org.springframework.lang.Nullable", GlobalSearchScope.allScope(project)));
            // Import the "org.springframework.lang.NonNull" annotation
            PsiImportStatement nonNullImport = elementFactory.createImportStatement(psiFacade.findClass("org.springframework.lang.NonNull", GlobalSearchScope.allScope(project)));
            PsiImportList importList = psiJavaFile.getImportList();
            PsiElement lastImport = importList.getLastChild();
            if (Arrays.stream(importList.getImportStatements()).noneMatch(importStatement -> importStatement.getQualifiedName().equals("org.springframework.lang.Nullable"))) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    importList.addAfter(nullableImport, lastImport);
                });
            }
            if (Arrays.stream(importList.getImportStatements()).noneMatch(importStatement -> importStatement.getQualifiedName().equals("org.springframework.lang.NonNull"))) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    importList.addAfter(nonNullImport, lastImport);
//                    psiJavaFile.addAfter(nonNullImport,lastImport);
                });
            }
            // get the document from the editor
            Document document = PsiDocumentManager.getInstance(project).getDocument(psiJavaFile);
            // save the changes
            WriteCommandAction.runWriteCommandAction(project, () -> PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document));
            PsiDocumentManager.getInstance(project).commitDocument(document);
            connection.close();
            Messages.showInfoMessage(project, "Null Notion processing complete", "Success");
        } catch (Exception ex) {
            logger.error(ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error");
        }
    }
}

