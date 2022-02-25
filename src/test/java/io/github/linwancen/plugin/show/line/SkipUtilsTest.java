package io.github.linwancen.plugin.show.line;


import groovy.json.JsonOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

/**
 * @see SkipUtils
 */
public class SkipUtilsTest {

    public static final boolean o = true;
    public static final boolean x = false;

    String[] names = {"java", "io.a", "io.b"};
    String[][] includes = {
            {},
            {"java"},
            {"io"},
            {"java", "io"},
    };
    String[][] excludes = {
            {},
            {"java"},
            {"io.b"},
            {"java", "io.b"},
    };

    @Test
    public void skipName() {
        // o include, x skip
        boolean[][][] results = {{
                // "java" -- name
                // {}, {"java"}, {"io.b"}, {"java", "io.b"} -- exclude
                {o, x, o, x}, // {},
                {o, x, o, x}, // {"java"},
                {x, x, x, x}, // {"io"},
                {o, x, o, x}, // {"java", "io"},
        }, {
                // "io.a" -- name
                // {}, {"java"}, {"io.b"}, {"java", "io.b"} -- exclude
                {o, o, o, o}, // {},
                {x, x, x, x}, // {"java"},
                {o, o, o, o}, // {"io"},
                {o, o, o, o}, // {"java", "io"},
        }, {
                // "io.b" -- name
                // {}, {"java"}, {"io.b"}, {"java", "io.b"} -- exclude
                {o, o, x, x}, // {},
                {x, x, x, x}, // {"java"},
                {o, o, x, x}, // {"io"},
                {o, o, x, x}, // {"java", "io"},
        }};
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            String name = names[i];
            loopTest(name, results[i]);
        }
    }

    private void loopTest(String name, boolean[][] results) {
        for (int includeIndex = 0, includesLength = includes.length; includeIndex < includesLength; includeIndex++) {
            String[] include = includes[includeIndex];
            for (int excludeIndex = 0, excludesLength = excludes.length; excludeIndex < excludesLength; excludeIndex++) {
                String[] exclude = excludes[excludeIndex];
                boolean isSkip = SkipUtils.skipName(name, include, exclude);
                String tip =
                        name + "==" + JsonOutput.toJson(include) + "!=" + JsonOutput.toJson(exclude) + "=>" + isSkip;
                System.out.println(tip);
                // o true include, x false skip, so use '!'
                Assertions.assertEquals(!results[includeIndex][excludeIndex], isSkip, tip);
            }
            System.out.println();
        }
    }

    @Test
    public void include() {
        boolean[][] results = {
                // {"java", "io.a", "io.b"} -- name
                {o, o, o}, // {},
                {o, x, x}, // {"java"},
                {x, o, o}, // {"io"},
                {o, o, o}, // {"java", "io"},
        };
        loopTest(SkipUtils::include, results);
    }

    @Test
    public void exclude() {
        boolean[][] results = {
                // {"java", "io.a", "io.b"} ... names
                {x, x, x}, // {},
                {o, x, x}, // {"java"},
                {x, o, o}, // {"io"},
                {o, o, o}, // {"java", "io"},
        };
        loopTest(SkipUtils::exclude, results);
    }

    private void loopTest(BiPredicate<String, String[]> biPredicate, boolean[][] results) {
        for (int includeIndex = 0, includesLength = includes.length; includeIndex < includesLength; includeIndex++) {
            String[] include = includes[includeIndex];
            for (int nameIndex = 0, namesLength = names.length; nameIndex < namesLength; nameIndex++) {
                String name = names[nameIndex];
                boolean result = biPredicate.test(name, include);
                String tip = name + "==" + JsonOutput.toJson(include) + "=>" + result;
                System.out.println(tip);
                Assertions.assertEquals(results[includeIndex][nameIndex], result, tip);
            }
            System.out.println();
        }
    }
}