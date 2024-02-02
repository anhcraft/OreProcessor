package dev.anhcraft.oreprocessor.api.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum MaterialClass {
    VANILLA(null),
    ORAXEN("ox"),
    ITEMSADDER("ia");

    private static final Map<String, MaterialClass> PREFIX_LOOKUP = new HashMap<>();

    @Nullable
    public static MaterialClass getClassByPrefix(@Nullable String prefix) {
        return PREFIX_LOOKUP.get(prefix);
    }

    static {
        for (MaterialClass materialClass : values()) {
            PREFIX_LOOKUP.put(materialClass.getPrefix(), materialClass);
        }
    }

    private final String prefix;

    MaterialClass(String prefix) {
        this.prefix = prefix;
    }

    @Nullable
    public String getPrefix() {
        return prefix;
    }
}
