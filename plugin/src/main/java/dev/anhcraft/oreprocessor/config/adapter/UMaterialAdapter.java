package dev.anhcraft.oreprocessor.config.adapter;

import dev.anhcraft.config.ConfigDeserializer;
import dev.anhcraft.config.ConfigSerializer;
import dev.anhcraft.config.adapters.TypeAdapter;
import dev.anhcraft.config.struct.SimpleForm;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

public class UMaterialAdapter implements TypeAdapter<UMaterial> {
    @Override
    public @Nullable SimpleForm simplify(@NotNull ConfigSerializer configSerializer, @NotNull Type type, @NotNull UMaterial uMaterial) throws Exception {
        return SimpleForm.of(uMaterial.toString());
    }

    @Override
    public @Nullable UMaterial complexify(@NotNull ConfigDeserializer configDeserializer, @NotNull Type type, @NotNull SimpleForm simpleForm) throws Exception {
        return UMaterial.parse(Objects.requireNonNull(simpleForm.asString()));
    }
}
