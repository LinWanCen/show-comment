package io.github.linwancen.plugin.show.java;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.XmlLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.MavenPropertyResolver;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenLangDoc extends XmlLangDoc {
    Pattern PARAM_PATTERN = Pattern.compile("\\$\\{[^}]++}");

    @Nullable
    @Override
    public String findRefDoc(@NotNull LineInfo info, @NotNull FileViewProvider viewProvider,
                             @NotNull PsiElement element) {
        if (!"pom.xml".equals(info.file.getName()) || !info.text.contains("$")) {
            return null;
        }
        MavenDomProjectModel mdm = MavenDomUtil.getMavenDomModel(element.getContainingFile(), MavenDomProjectModel.class);
        if (mdm == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        Matcher m = PARAM_PATTERN.matcher(info.text);
        while (m.find()) {
            String s = m.group();
            String v;
            try {
                v = MavenPropertyResolver.resolve(s, mdm);
            } catch (Exception ignored) {
                v = "";
            }
            list.add(v);
        }
        if (list.isEmpty()) {
            return null;
        }
        return String.join(" | ", list);
    }
}
