package io.github.linwancen.plugin.show.json;

import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private JsonUtils() {}

    @NotNull
    public static List<String> jsonPath(JsonProperty jsonProp) {
        ArrayList<String> jsonPath = new ArrayList<>();
        do {
            jsonPath.add(jsonProp.getName());
        } while ((jsonProp = PsiTreeUtil.getParentOfType(jsonProp, JsonProperty.class)) != null);
        return jsonPath;
    }
}
