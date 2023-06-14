package dev.anhcraft.oreprocessor.api;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents an ore.
 */
public class Ore {
    private final String id;
    private final String name;
    private final Material icon;
    private final Set<Material> blocks;
    private final Map<String, OreTransform> transform;
    private final Set<Material> acceptableFeedstock;

    public Ore(String id, String name, Material icon, Set<Material> blocks, Map<String, OreTransform> transform, Set<Material> acceptableFeedstock) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.blocks = blocks; // unmodifiable
        this.transform = transform; // unmodifiable
        this.acceptableFeedstock = acceptableFeedstock; // unmodifiable
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public Set<Material> getBlocks() {
        return blocks; // unmodifiable
    }

    @NotNull
    public Set<String> getTransformIds() {
        return transform.keySet(); // unmodifiable
    }

    @Nullable
    public OreTransform getTransform(String id) {
        return transform.get(id);
    }

    @NotNull
    public OreTransform getDefaultTransform() {
        return Objects.requireNonNull(transform.get("default"), "Default transform not found");
    }

    @NotNull
    public OreTransform getBestTransform(@NotNull UUID player) {
        return getBestTransform(Bukkit.getOfflinePlayer(player));
    }

    /**
     * Returns the best ore transformation for given player.<br>
     * If the player is offline, the default one is always selected. Otherwise, this performs permission tests from<br>
     * the bottom transformation in the config (indicating the best) upwards to the top one. If none is found,<br>
     * the default one is returned.<br>
     * The permission format is: {@code oreprocessor.transform.<ORE ID>.<TRANSFORM ID>}
     * @param player the player
     * @return the best transformation
     */
    @NotNull
    public OreTransform getBestTransform(@NotNull OfflinePlayer player) {
        if (!(player instanceof Player))
            return getDefaultTransform();

        Player p = (Player) player;

        List<String> reversedKeys = new ArrayList<>(transform.keySet());
        Collections.reverse(reversedKeys);
        for (String key : reversedKeys) {
            if (p.hasPermission("oreprocessor.transform." + getId() + "." + key)) {
                return transform.get(key);
            }
        }

        return getDefaultTransform();
    }

    @NotNull
    public Set<Material> getAcceptableFeedstock() {
        return acceptableFeedstock; // unmodifiable
    }

    public boolean isAcceptableFeedstock(Material material) {
        return acceptableFeedstock.contains(material);
    }
}
