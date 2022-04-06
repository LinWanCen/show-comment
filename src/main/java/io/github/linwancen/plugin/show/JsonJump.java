package io.github.linwancen.plugin.show;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.github.linwancen.plugin.show.doc.PsiClassUtils;
import io.github.linwancen.plugin.show.json.JsonRef;
import io.github.linwancen.plugin.show.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonJump extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonStringLiteral.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        JsonProperty jsonProp = PsiTreeUtil.getParentOfType(element, JsonProperty.class, true);
                        if (jsonProp == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
                        if (virtualFile == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        Project project = element.getProject();
                        List<PsiField> psiFields = new ArrayList<>();
                        List<PsiField> tips = new ArrayList<>();
                        PsiClass[] psiClasses = PsiClassUtils.encClass(virtualFile, project);
                        List<String> jsonPath = JsonUtils.jsonPath(jsonProp);
                        put(project, psiFields, tips, psiClasses, jsonPath, jsonPath.size() - 1);

                        List<PsiReference> list = new ArrayList<>();
                        for (PsiField psiField : psiFields) {
                            list.add(new JsonRef(element, psiField, tips));
                        }
                        return list.toArray(new PsiReference[0]);
                    }
                });
    }

    private static void put(Project project, List<PsiField> psiFields, List<PsiField> tips,
                            PsiClass[] psiClasses, List<String> jsonPath, int level) {
        String name = jsonPath.get(level);
        for (PsiClass psiClass : psiClasses) {
            if (level == 1) {
                tips.addAll(Arrays.asList(psiClass.getAllFields()));
            }
            PsiField psiField = psiClass.findFieldByName(name, true);
            if (psiField == null) {
                continue;
            }
            if (level == 0) {
                psiFields.add(psiField);
            } else {
                String classFullName = PsiClassUtils.toClassFullName(psiField);
                PsiClass[] classes = PsiClassUtils.fullNameToClass(classFullName, project);
                put(project, psiFields, tips, classes, jsonPath, level - 1);
            }
        }
    }
}