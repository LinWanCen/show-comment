package io.github.linwancen.plugin.show.demo.java.obj;

import java.util.LinkedHashMap;

public enum YesOrNoEnum {
    YES("1", "是"),
    NO("0", "否"),
    ;

    public final String code;
    public final String desc;

    YesOrNoEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return code + '-' + desc;
    }

    private static final LinkedHashMap<String, YesOrNoEnum> map;

    static {
        map = new LinkedHashMap<>();
        for (YesOrNoEnum value : values()) {
            map.put(value.code, value);
        }
    }

    public static YesOrNoEnum fromCode(String code) {
        return map.get(code);
    }
}