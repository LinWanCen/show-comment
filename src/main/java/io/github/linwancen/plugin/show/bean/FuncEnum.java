package io.github.linwancen.plugin.show.bean;

import java.util.LinkedHashMap;

public enum FuncEnum {
    /** tree: project view tree  */
    TREE("tree", "project view tree"),
    /** line: code line end */
    LINE("line", "code line end"),
    ;

    public final String code;
    public final String desc;

    FuncEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return code + '-' + desc;
    }

    private static final LinkedHashMap<String, FuncEnum> map;

    static {
        map = new LinkedHashMap<>();
        for (FuncEnum value : values()) {
            map.put(value.code, value);
        }
    }

    public static FuncEnum fromCode(String code) {
        return map.get(code);
    }
}