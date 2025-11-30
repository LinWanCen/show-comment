package io.github.linwancen.plugin.show.java;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.github.linwancen.plugin.show.java.doc.PsiClassUtils;
import io.github.linwancen.plugin.show.jump.JsonRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonJumpJava extends PsiReferenceContributor {

    private static final Logger LOG = LoggerFactory.getLogger(JsonJumpJava.class);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        try {
            register(registrar);
        } catch (Exception e) {
            LOG.info("JsonJumpJava catch Throwable but log to record.", e);
        }
    }

    private static void register(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonStringLiteral.class),
                new PsiReferenceProvider() {
                    @Override
                    public @NotNull PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                          @NotNull ProcessingContext context) {
                        try {
                            @Nullable JsonProperty jsonProp = PsiTreeUtil.getParentOfType(
                                    element, JsonProperty.class, true);
                            if (jsonProp == null) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
                            if (virtualFile == null) {
                                return PsiReference.EMPTY_ARRAY;
                            }

                            @NotNull Project project = element.getProject();
                            @NotNull List<PsiField> psiFields = new ArrayList<>();
                            @NotNull List<PsiField> tips = new ArrayList<>();
                            return DumbService.getInstance(project).runReadActionInSmartMode(() -> ReadAction.compute(() -> {
                                @NotNull PsiClass[] psiClasses = PsiClassUtils.fileToClasses(virtualFile, project);
                                @NotNull List<String> jsonPath = jsonPath(jsonProp);
                                put(project, psiFields, tips, psiClasses, jsonPath, jsonPath.size() - 1);

                                @NotNull List<PsiReference> list = new ArrayList<>();
                                for (@NotNull PsiField psiField : psiFields) {
                                    list.add(new JsonRef<>(element, psiField, tips));
                                }
                                return list;
                            })).toArray(PsiReference.EMPTY_ARRAY);
                        } catch (ProcessCanceledException ignored) {
                            return PsiReference.EMPTY_ARRAY;
                        } catch (Throwable e) {
                            LOG.error("JsonJumpJava.register catch Throwable but log to record.", e);
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });
    }

    @NotNull
    private static List<String> jsonPath(@NotNull JsonProperty jsonProp) {
        @NotNull ArrayList<String> jsonPath = new ArrayList<>();
        do {
            jsonPath.add(jsonProp.getName());
        } while ((jsonProp = PsiTreeUtil.getParentOfType(jsonProp, JsonProperty.class)) != null);
        return jsonPath;
    }

    private static void put(@NotNull Project project, @NotNull List<PsiField> psiFields, @NotNull List<PsiField> tips,
                            @NotNull PsiClass[] psiClasses, @NotNull List<String> jsonPath, int level) {
        String name = jsonPath.get(level);
        for (@NotNull PsiClass psiClass : psiClasses) {
            if (level == 1) {
                tips.addAll(Arrays.asList(psiClass.getAllFields()));
            }
            @Nullable PsiField psiField = psiClass.findFieldByName(name, true);
            if (psiField == null) {
                continue;
            }
            if (level == 0) {
                psiFields.add(psiField);
            } else {
                @NotNull String classFullName = PsiClassUtils.toClassFullName(psiField);
                @NotNull PsiClass[] classes = PsiClassUtils.fullNameToClass(classFullName, project);
                put(project, psiFields, tips, classes, jsonPath, level - 1);
            }
        }
    }
}