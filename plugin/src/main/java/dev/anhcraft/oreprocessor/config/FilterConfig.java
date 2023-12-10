package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.annotations.Validation;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configurable
public class FilterConfig {
    @Description("The list of items to be whitelisted in the storage.")
    @Validation(notNull = true)
    public Map<String, List<Material>> storage;

    @PostHandler
    private void handle() {
        for (List<Material> value : storage.values()) {
            value.removeIf(Objects::isNull);
        }
    }
}
