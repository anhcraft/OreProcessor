package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.ConfigDeserializer;
import dev.anhcraft.config.ConfigSerializer;
import dev.anhcraft.config.adapters.TypeAdapter;
import dev.anhcraft.config.struct.SimpleForm;
import dev.anhcraft.oreprocessor.util.OreTransform;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class OreTransformAdapter implements TypeAdapter<OreTransform> {
    public static final OreTransformAdapter INSTANCE = new OreTransformAdapter();

    @Override
    public @Nullable SimpleForm simplify(@NotNull ConfigSerializer configSerializer, @NotNull Type type, @NotNull OreTransform oreTransform) throws Exception {
        return SimpleForm.of(oreTransform.getRaw().name().toLowerCase() + " -> " + oreTransform.getProduct().name().toLowerCase());
    }

    @Override
    public @Nullable OreTransform complexify(@NotNull ConfigDeserializer configDeserializer, @NotNull Type type, @NotNull SimpleForm simpleForm) throws Exception {
        if (simpleForm.isString()) {
            String[] s = simpleForm.asString().split("->");
            if (s.length == 2) {
                return new OreTransform(
                        Material.valueOf(s[0].toUpperCase()),
                        Material.valueOf(s[1].toUpperCase())
                );
            }
        }
        return null;
    }
}
