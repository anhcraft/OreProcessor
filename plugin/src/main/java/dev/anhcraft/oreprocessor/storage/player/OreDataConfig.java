package dev.anhcraft.oreprocessor.storage.player;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.PostHandler;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

@Configurable
class OreDataConfig {

    public int throughput;

    public int capacity;

    @Nullable
    public LinkedHashMap<Material, Integer> feedstock;

    @Nullable
    public LinkedHashMap<Material, Integer> products;

    @PostHandler
    private void handle() {
        if (feedstock != null)
            feedstock.keySet().removeIf(material -> feedstock.get(material) <= 0);
        if (products != null)
            products.keySet().removeIf(material -> products.get(material) <= 0);
    }
}
