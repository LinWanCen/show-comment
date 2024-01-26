package io.github.linwancen.plugin.show.java.doc;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiConstantEvaluationHelper;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJvmModifiersOwner;
import com.intellij.psi.PsiMethod;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnnoDoc {

    @Nullable
    public static <T extends SettingsInfo> String annoDoc(@NotNull T info, @NotNull PsiJvmModifiersOwner owner) {
        @NotNull ProjectSettingsState projectSettings = info.projectSettings;
        @NotNull GlobalSettingsState globalSettings = info.globalSettings;
        // annoDocEffect first because default false
        if (projectSettings.annoDocEffect && projectSettings.projectFilterEffective) {
            @Nullable String doc = annoDocArr(owner, projectSettings.annoDoc);
            if (StringUtils.isNotBlank(doc)) {
                return doc;
            }
        }
        if (globalSettings.annoDocEffect && projectSettings.globalFilterEffective) {
            return annoDocArr(owner, globalSettings.annoDoc);
        }
        return null;
    }

    @Nullable
    private static String annoDocArr(@NotNull PsiJvmModifiersOwner owner, @NotNull String[][] lines) {
        for (@NotNull String[] arr : lines) {
            if (arr.length < 3) {
                continue;
            }
            @Nullable String s = annoDocMatch(owner, arr);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    @Nullable
    private static String annoDocMatch(@NotNull PsiJvmModifiersOwner owner, @NotNull String[] arr) {
        if (typeMatch(owner, arr[0])) {
            return annoDocName(owner, arr);
        }
        if (owner instanceof PsiMethod && "field".equals(arr[0])) {
            @Nullable PsiField psiField = PsiMethodToPsiDoc.propMethodField((PsiMethod) owner);
            if (psiField != null) {
                return annoDocName(psiField, arr);
            }
        }
        return null;
    }

    private static boolean typeMatch(PsiJvmModifiersOwner owner, @NotNull String type) {
        switch (type) {
            case "field":
                return owner instanceof PsiField;
            case "method":
                return owner instanceof PsiMethod;
            case "class":
                return owner instanceof PsiClass;
            case "!doc":
                return !(owner instanceof PsiDocCommentOwner);
            case "all":
                return true;
            default:
                return false;
        }
    }

    @Nullable
    private static String annoDocName(@NotNull PsiJvmModifiersOwner owner, @NotNull String[] arr) {
        @Nullable PsiAnnotation annotation = owner.getAnnotation(arr[1]);
        if (annotation == null) {
            return null;
        }
        for (int i = 2; i < arr.length; i++) {
            @Nullable PsiAnnotationMemberValue value = annotation.findAttributeValue(arr[i]);
            @Nullable String s = annoDocValue(value);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    @Nullable
    private static String annoDocValue(@Nullable PsiAnnotationMemberValue value) {
        if (value == null) {
            return null;
        }
        @NotNull Project project = value.getProject();
        @NotNull PsiConstantEvaluationHelper helper = JavaPsiFacade.getInstance(project).getConstantEvaluationHelper();
        @Nullable Object o = helper.computeConstantExpression(value);
        if (o == null) {
            return null;
        }
        String doc = o.toString();
        if (doc.isEmpty()) {
            return null;
        }
        return doc;
    }
}
