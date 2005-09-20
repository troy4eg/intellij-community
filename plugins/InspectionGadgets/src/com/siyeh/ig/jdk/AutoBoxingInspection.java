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
package com.siyeh.ig.jdk;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.ExpectedTypeUtils;
import com.siyeh.InspectionGadgetsBundle;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NonNls;

public class AutoBoxingInspection extends ExpressionInspection {
    /** @noinspection StaticCollection*/
    @NonNls private static final Map<String,String> s_boxingClasses = new HashMap<String, String>(8);
    private final AutoBoxingFix fix = new AutoBoxingFix();

    static {
      s_boxingClasses.put("int", "Integer");
      s_boxingClasses.put("short", "Short");
      s_boxingClasses.put("boolean", "Boolean");
      s_boxingClasses.put("long", "Long");
      s_boxingClasses.put("byte", "Byte");
      s_boxingClasses.put("float", "Float");
      s_boxingClasses.put("double", "Double");
      s_boxingClasses.put("char", "Character");
    }

  public String getDisplayName() {
      return InspectionGadgetsBundle.message("auto.boxing.display.name");
  }

    public String getGroupDisplayName() {
        return GroupNames.JDK_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
      return InspectionGadgetsBundle.message("auto.boxing.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new AutoBoxingVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class AutoBoxingFix extends InspectionGadgetsFix {
        public String getName() {
            return InspectionGadgetsBundle.message("auto.boxing.make.boxing.explicit.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiExpression expression = (PsiExpression) descriptor.getPsiElement();
            final PsiType expectedType = ExpectedTypeUtils.findExpectedType(expression, false);
            assert expectedType != null;
            final String newExpression;
            if (expectedType.equals(PsiType.BOOLEAN)) {
              @NonNls final String booleanValueOf = "Boolean.valueOf";
              newExpression = booleanValueOf + "(" + expression.getText() + ')';
            } else if (s_boxingClasses.containsValue(expectedType.getPresentableText())) {
                final String classToConstruct = expectedType.getPresentableText();
                newExpression = PsiKeyword.NEW + " " + classToConstruct + '(' + expression.getText() + ')';
            } else {
                final String classToConstruct = s_boxingClasses.get(expression.getType().getPresentableText());
                newExpression = PsiKeyword.NEW + " " + classToConstruct + '(' + expression.getText() + ')';
            }
            replaceExpression(expression, newExpression);
        }
    }

    private static class AutoBoxingVisitor extends BaseInspectionVisitor {

        public void visitBinaryExpression(PsiBinaryExpression expression) {
            super.visitBinaryExpression(expression);
            checkExpression(expression);
        }

        public void visitConditionalExpression(PsiConditionalExpression expression)
        {
            super.visitConditionalExpression(expression);
            checkExpression(expression);
        }

        public void visitLiteralExpression(PsiLiteralExpression expression) {
            super.visitLiteralExpression(expression);
            checkExpression(expression);
        }

        public void visitPostfixExpression(PsiPostfixExpression expression) {
            super.visitPostfixExpression(expression);
            checkExpression(expression);
        }

        public void visitPrefixExpression(PsiPrefixExpression expression) {
            super.visitPrefixExpression(expression);
            checkExpression(expression);
        }

        public void visitReferenceExpression(PsiReferenceExpression expression) {
            super.visitReferenceExpression(expression);
            checkExpression(expression);
        }

        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            checkExpression(expression);
        }

        public void visitTypeCastExpression(PsiTypeCastExpression expression) {
            super.visitTypeCastExpression(expression);
            checkExpression(expression);
        }

        public void visitAssignmentExpression(PsiAssignmentExpression expression) {
            super.visitAssignmentExpression(expression);
            checkExpression(expression);
        }

        public void visitParenthesizedExpression(PsiParenthesizedExpression expression) {
            super.visitParenthesizedExpression(expression);
            checkExpression(expression);
        }

        private void checkExpression(PsiExpression expression) {
            final PsiType expressionType = expression.getType();
            if(expressionType == null) {
                return;
            }
            if(expressionType.equals(PsiType.VOID)) {
                return;
            }
            if(!ClassUtils.isPrimitive(expressionType)) {
                return;
            }
            final PsiType expectedType =
                    ExpectedTypeUtils.findExpectedType(expression, false);
            if(expectedType == null) {
                return;
            }

            if(ClassUtils.isPrimitive(expectedType)) {
                return;
            }
            registerError(expression);
        }
    }
}