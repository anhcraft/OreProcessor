package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.oreprocessor.api.util.UMaterial;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configurable
public class FilterConfig {
    @Description("The list of items to be whitelisted in the storage.")
    @Validation(notNull = true)
    public Map<String, List<UMaterial>> storage;

    @PostHandler
    private void handle() {
        for (List<UMaterial> value : storage.values()) {
            value.removeIf(Objects::isNull);
        }
    }
}
