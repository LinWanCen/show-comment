package io.github.linwancen.plugin.show.ext.ollama;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Open Ollama models dir
 */
public class OllamaModelsCache extends FileLoader {
    private static final Logger LOG = LoggerFactory.getLogger(OllamaModelsCache.class);

    @Override
    public boolean skipFile(@NotNull VirtualFile file) {
        return !file.getPath().contains("/manifests/registry.ollama.ai");
    }

    @Override
    public void loadAllImpl(@NotNull Project project) {
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        @Nullable VirtualFile dir = fileSystem.findFileByPath(project.getBasePath() + "/manifests/registry.ollama.ai");
        if (dir == null) {
            return;
        }
        @NotNull StringBuilder sb = new StringBuilder();
        visitChildrenRecursively(project, dir, sb);
        LOG.info("ollama manifests load all complete files\n{}", sb);
    }

    @Override
    public void loadFileImpl(@NotNull VirtualFile file, @Nullable Project project) {
        if (project == null) {
            return;
        }
        try {
            VirtualFile parent = file.getParent();
            if (parent == null) {
                return;
            }
            @NotNull String name = parent.getName();
            @NotNull String version = file.getName();
            @NotNull String id = name + ':' + version;

            @NotNull String s = new String(file.contentsToByteArray(), file.getCharset());
            JsonElement jsonElement = JsonParser.parseString(s);
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                return;
            }
            JsonObject json = jsonElement.getAsJsonObject();
            JsonObject config = json.getAsJsonObject("config");
            load(project, config, id);
            JsonArray layers = json.getAsJsonArray("layers");
            if (layers == null) {
                return;
            }
            layers.forEach(layer -> load(project, layer.getAsJsonObject(), id));
        } catch (IOException e) {
            LOG.warn("ollama manifests load {} fail: ", file.getPath(), e);
        }
    }

    private void load(@NotNull Project project, @Nullable JsonObject config, String id) {
        if (config == null) {
            return;
        }
        JsonElement digest = config.get("digest");
        if (digest == null) {
            return;
        }
        @NotNull String fileName = digest.getAsString().replace(':', '-');
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        @Nullable VirtualFile file = fileSystem.findFileByPath(project.getBasePath() + "/blobs/" + fileName);
        if (file == null) {
            return;
        }
        JsonElement mediaType = config.get("mediaType");
        if (mediaType == null) {
            fileDoc.put(file, id);
            return;
        }
        String type = mediaType.getAsString();
        if (type.endsWith("json")) {
            type = type.substring(0, type.lastIndexOf('.'));
        }
        @NotNull String typeEnd = type.substring(type.lastIndexOf('.') + 1);
        fileDoc.put(file, id + " | " + typeEnd);
    }
}
