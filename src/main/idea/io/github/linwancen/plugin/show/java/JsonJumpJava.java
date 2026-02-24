package io.github.linwancen.plugin.show.java;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
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
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.github.linwancen.plugin.show.java.doc.PsiClassUtils;
import io.github.linwancen.plugin.show.jump.JsonRef;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import io.github.linwancen.util.StrStyleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
                            @NotNull Project project = element.getProject();
                            if (DumbService.getInstance(project).isDumb()) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            @Nullable
                            JsonProperty jsonProp = PsiTreeUtil.getParentOfType(element, JsonProperty.class, true);
                            if (jsonProp == null) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            @Nullable String value = value(jsonProp, element);
                            if (value != null) {
                                @NotNull PsiReference[] classRef = classRef(element, value);
                                if (classRef.length > 0) {
                                    return classRef;
                                }
                            }

                            VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
                            if (virtualFile == null) {
                                return PsiReference.EMPTY_ARRAY;
                            }

                            @NotNull List<PsiField> psiFields = new ArrayList<>();
                            @NotNull List<Object> variants = new ArrayList<>();
                            @NotNull PsiClass[] psiClasses = PsiClassUtils.jsonFileToClasses(virtualFile, project);
                            @NotNull List<String> jsonPath = jsonPath(jsonProp);
                            put(project, psiFields, variants, psiClasses, jsonPath, jsonPath.size() - 1);

                            @NotNull List<PsiReference> list = new ArrayList<>();
                            for (@NotNull PsiField psiField : psiFields) {
                                if (value != null) {
                                    enumRef(list, element, value, psiField);
                                } else {
                                    list.add(new JsonRef<>(element, psiField, variants));
                                }
                            }
                            return list.toArray(PsiReference.EMPTY_ARRAY);
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

    private static void put(@NotNull Project project, @NotNull List<PsiField> psiFields,
                            @NotNull List<Object> variants,
                            @NotNull PsiClass[] psiClasses, @NotNull List<String> jsonPath, int level) {
        String name = jsonPath.get(level);
        for (@NotNull PsiClass psiClass : psiClasses) {
            if (level == 1) {
                variants(psiClass.getAllFields(), variants);
            }
            @Nullable PsiField psiField;
            if (name.indexOf('_') < 0) {
                psiField = psiClass.findFieldByName(name, true);
            } else {
                String s = StrStyleUtils.snakeToCamel(name);
                psiField = psiClass.findFieldByName(s, true);
                if (psiField == null) {
                    psiField = psiClass.findFieldByName(name, true);
                }
            }
            if (psiField == null) {
                continue;
            }
            if (level == 0) {
                psiFields.add(psiField);
            } else {
                @NotNull String classFullName = PsiClassUtils.toClassFullName(psiField);
                @NotNull PsiClass[] classes = PsiClassUtils.fullNameToClass(classFullName, project);
                put(project, psiFields, variants, classes, jsonPath, level - 1);
            }
        }
    }

    @Nullable
    private static String value(@NotNull JsonProperty jsonProp, PsiElement element) {
        PsiElement firstChild = jsonProp.getFirstChild();
        if (element == firstChild) {
            return null;
        }
        if (!(element instanceof JsonStringLiteral)) {
            return null;
        }
        @NotNull JsonStringLiteral jsonStringLiteral = (JsonStringLiteral) element;
        return jsonStringLiteral.getValue();
    }

    @NotNull
    private static PsiReference[] classRef(@NotNull PsiElement element, String value) {
        @NotNull PsiClass[] psiClasses = PsiClassUtils.nameToClass(value, element.getProject());
        if (psiClasses.length > 0) {
            @NotNull List<Object> variants = new ArrayList<>();
            @NotNull List<PsiReference> list = new ArrayList<>();
            for (@NotNull PsiClass psiClass : psiClasses) {
                list.add(new JsonRef<>(element, psiClass, variants));
            }
            return list.toArray(PsiReference.EMPTY_ARRAY);
        }
        return PsiReference.EMPTY_ARRAY;
    }

    private static void enumRef(@NotNull List<PsiReference> list, @NotNull PsiElement element, @NotNull String value,
                                @NotNull PsiField psiField) {
        // Jump Kotlin Element
        PsiElement navigationElement = psiField.getNavigationElement();
        if (navigationElement == null) {
            navigationElement = psiField;
        }
        @NotNull String text = PsiUnSaveUtils.getText(navigationElement);
        int l = text.indexOf('=');
        if (l > 0) {
            @NotNull String init = text.substring(l + 1).trim();
            int i = init.indexOf('.');
            if (i > 0) {
                @NotNull String className = init.substring(0, i).trim();
                @NotNull PsiClass[] psiClasses = PsiClassUtils.nameToClass(className, element.getProject());
                for (@NotNull PsiClass psiClass : psiClasses) {
                    if (addEnum(list, element, value, psiClass)) {
                        return;
                    }
                }
            }
        }

        @Nullable PsiTypeElement typeElement = psiField.getTypeElement();
        if (typeElement == null) {
            return;
        }
        @NotNull PsiElement[] children = typeElement.getChildren();
        if (children.length == 0) {
            return;
        }
        PsiElement ref = children[0];
        if (!(ref instanceof PsiJavaCodeReferenceElementImpl)) {
            return;
        }
        @NotNull PsiJavaCodeReferenceElementImpl javaRef = (PsiJavaCodeReferenceElementImpl) ref;
        @Nullable PsiElement resolve = null;
        try {
            resolve = javaRef.resolve();
        } catch (Throwable ignore) {
        }
        if (!(resolve instanceof PsiClass)) {
            return;
        }
        @NotNull PsiClass psiClass = (PsiClass) resolve;
        addEnum(list, element, value, psiClass);
    }

    private static boolean addEnum(@NotNull List<PsiReference> list, @NotNull PsiElement element,
                                   @NotNull String value, @NotNull PsiClass psiClass) {
        if (!psiClass.isEnum()) {
            return true;
        }
        @NotNull PsiField[] fields = psiClass.getFields();
        @NotNull List<Object> variants = new ArrayList<>();
        variants(fields, variants);
        for (@NotNull PsiField field : fields) {
            if (value.equals(field.getName())) {
                list.add(new JsonRef<>(element, field, variants));
            }
        }
        if (list.isEmpty()) {
            @NotNull String str = '"' + value + '"';
            @NotNull String prefix = '(' + value + ',';
            for (@NotNull PsiField field : fields) {
                String text = field.getText();
                if (text.contains(str) || text.contains(prefix)) {
                    list.add(new JsonRef<>(element, field, variants));
                }
            }
        }
        return false;
    }

    private static void variants(@NotNull PsiField[] psiFields, @NotNull List<Object> variants) {
        for (@NotNull PsiField psiField : psiFields) {
            variants.add(psiField.getName());
        }
    }
}