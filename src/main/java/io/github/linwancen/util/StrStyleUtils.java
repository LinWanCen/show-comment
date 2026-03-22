package io.github.linwancen.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StrStyleUtils {
    /**
     * @author DeepSeek
     */
    public static String snakeToCamel(@Nullable String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        @NotNull StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < snakeCase.length(); i++) {
            char currentChar = snakeCase.charAt(i);
            if (currentChar == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return result.toString();
    }
}
