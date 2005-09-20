/*
 * Copyright 2003-2005 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.style;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.FileInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnnecessarySemicolonInspection extends FileInspection{
    private final UnnecessarySemicolonFix fix = new UnnecessarySemicolonFix();

    public String getGroupDisplayName(){
        return GroupNames.STYLE_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }

    public String buildErrorString(PsiElement location){
        return InspectionGadgetsBundle.message("unnecessary.semicolon.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor(){
        return new UnnecessarySemicolonVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private static class UnnecessarySemicolonFix extends InspectionGadgetsFix{
        public String getName(){
            return InspectionGadgetsBundle.message("unnecessary.semicolon.remove.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException{
            final PsiElement semicolonElement = descriptor.getPsiElement();
            final PsiElement parent = semicolonElement.getParent();
            if(parent instanceof PsiEmptyStatement){
                final PsiElement lastChild = parent.getLastChild();
                if(lastChild instanceof PsiComment){
                    parent.replace(lastChild);
                } else{
                    deleteElement(parent);
                }
            } else{
                deleteElement(semicolonElement);
            }
        }
    }

    private static class UnnecessarySemicolonVisitor
            extends BaseInspectionVisitor{

        public void visitFile(PsiFile file){
            final PsiElement firstChild = file.getFirstChild();
            PsiElement sibling = skipForwardWhiteSpacesAndComments(firstChild);
            while(sibling != null){
                if(sibling instanceof PsiJavaToken){
                    final PsiJavaToken token = (PsiJavaToken)sibling;
                    final IElementType tokenType = token.getTokenType();
                    if(tokenType.equals(JavaTokenType.SEMICOLON)){
                        registerError(sibling);
                    }
                }
                sibling = skipForwardWhiteSpacesAndComments(sibling);
            }
            super.visitFile(file);
        }

        public void visitClass(@NotNull PsiClass aClass){
            super.visitClass(aClass);

            PsiElement child = aClass.getFirstChild();
            while(child != null){
                if(child instanceof PsiJavaToken){
                    final PsiJavaToken token = (PsiJavaToken)child;
                    final IElementType tokenType = token.getTokenType();
                    if(tokenType.equals(JavaTokenType.SEMICOLON)){
                        final PsiElement prevSibling =
                                skipBackwardWhiteSpacesAndComments(child);
                        if(!(prevSibling instanceof PsiEnumConstant)){
                            registerError(child);
                        }
                    }
                }
                child = skipForwardWhiteSpacesAndComments(child);
            }

            if(!aClass.isEnum()){
                return;
            }
            final PsiField[] fields = aClass.getFields();
            if(fields.length <= 0){
                return;
            }
            final PsiField lastField = fields[fields.length - 1];
            if(!(lastField instanceof PsiEnumConstant)){
                return;
            }
            final PsiElement element =
                    skipForwardWhiteSpacesAndComments(lastField);
            if(!(element instanceof PsiJavaToken)){
                return;
            }
            final PsiJavaToken token = (PsiJavaToken)element;
            final IElementType tokenType = token.getTokenType();
            if(!tokenType.equals(JavaTokenType.SEMICOLON)){
                return;
            }
            final PsiElement next = skipForwardWhiteSpacesAndComments(element);
            if(next == null || !next.equals(aClass.getRBrace())){
                return;
            }
            registerError(element);
        }

        @Nullable
        private static PsiElement skipForwardWhiteSpacesAndComments(
                PsiElement element){
            return PsiTreeUtil.skipSiblingsForward(element,
                                                   new Class[]{
                                                       PsiWhiteSpace.class,
                                                       PsiComment.class
                                                   });
        }

        @Nullable
        private static PsiElement skipBackwardWhiteSpacesAndComments(
                PsiElement element){
            return PsiTreeUtil.skipSiblingsBackward(element,
                                                    new Class[]{
                                                            PsiWhiteSpace.class,
                                                            PsiComment.class
                                                    });
        }

        public void visitEmptyStatement(PsiEmptyStatement statement){
            super.visitEmptyStatement(statement);
            final PsiElement parent = statement.getParent();
            if(parent instanceof PsiCodeBlock){
                final PsiElement semicolon = statement.getFirstChild();
                registerError(semicolon);
            }
        }
    }
}