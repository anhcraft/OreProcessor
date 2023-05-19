package dev.anhcraft.oreprocessor.storage.data;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
class OreDataConfig {
    @Exclude
    public final AtomicBoolean dirty = new AtomicBoolean(false);

    public int throughput;

    public int capacity;

    @Nullable
    public LinkedHashMap<Material, Integer> feedstock;

    @Nullable
    public LinkedHashMap<Material, Integer> products;
}
