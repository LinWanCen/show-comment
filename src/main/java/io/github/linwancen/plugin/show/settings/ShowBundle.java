package io.github.linwancen.plugin.show.settings;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class ShowBundle extends DynamicBundle {
    @NonNls
    public static final String BUNDLE = "messages.ShowCommentBundle";
    private static final ShowBundle INSTANCE = new ShowBundle();

    private ShowBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                 @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    @NotNull
    public static Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                                  @NotNull Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
