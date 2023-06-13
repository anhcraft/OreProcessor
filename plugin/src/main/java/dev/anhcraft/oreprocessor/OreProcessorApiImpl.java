package dev.anhcraft.oreprocessor;

import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreProcessorApi;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import dev.anhcraft.oreprocessor.api.upgrade.UpgradeLevel;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevelConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class OreProcessorApiImpl implements OreProcessorApi {
    private final OreProcessor plugin;
    private Map<String, Ore> ores;
    private TreeMap<Integer, UpgradeLevel> throughputUpgrades;
    private TreeMap<Integer, UpgradeLevel> capacityUpgrade;

    public OreProcessorApiImpl(OreProcessor plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        Map<String, Ore> oreMap = new HashMap<>();

        for (Map.Entry<String, OreConfig> entry : plugin.mainConfig.ores.entrySet()) {
            OreConfig oreConfig = entry.getValue();
            LinkedHashMap<String, OreTransform> transformMap = new LinkedHashMap<>();
            for (Map.Entry<String, Map<Material, Material>> e : oreConfig.transform.entrySet()) {
                transformMap.put(e.getKey(), new OreTransform(Collections.unmodifiableMap(e.getValue())));
            }

            oreMap.put(entry.getKey(), new Ore(
                    entry.getKey(),
                    oreConfig.name,
                    oreConfig.icon,
                    Collections.unmodifiableSet(oreConfig.blocks),
                    Collections.unmodifiableMap(transformMap)
            ));
        }

        ores = Collections.unmodifiableMap(oreMap);

        throughputUpgrades = new TreeMap<>();
        for (UpgradeLevelConfig upgradeLevelConfig : plugin.upgradeConfig.throughputUpgrade.values()) {
            throughputUpgrades.put(upgradeLevelConfig.amount, new UpgradeLevel(upgradeLevelConfig.amount, upgradeLevelConfig.cost));
        }

        capacityUpgrade = new TreeMap<>();
        for (UpgradeLevelConfig upgradeLevelConfig : plugin.upgradeConfig.capacityUpgrade.values()) {
            capacityUpgrade.put(upgradeLevelConfig.amount, new UpgradeLevel(upgradeLevelConfig.amount, upgradeLevelConfig.cost));
        }
    }

    @Override
    public @NotNull List<String> getOres() {
        if (ores == null)
            throw new UnsupportedOperationException("API is not ready yet");
        return new ArrayList<>(ores.keySet());
    }

    @Override
    public @Nullable Ore getOre(@NotNull String id) {
        if (ores == null)
            throw new UnsupportedOperationException("API is not ready yet");
        return ores.get(id);
    }

    @Override
    public int getProcessingSpeed() {
        return plugin.mainConfig.processingSpeed;
    }

    @Override
    public int getDefaultCapacity() {
        return plugin.upgradeConfig.capacityUpgrade.get("default").amount;
    }

    @Override
    public int getDefaultThroughput() {
        return plugin.upgradeConfig.throughputUpgrade.get("default").amount;
    }

    @Override
    public int getThroughputPerMinute(int throughput) {
        return (int) (throughput * 60d / OreProcessor.getApi().getProcessingSpeed());
    }

    @Override
    public @NotNull IPlayerData getPlayerData(@NotNull Player player) {
        return plugin.playerDataManager.getData(player);
    }

    @Override
    public @NotNull Optional<IPlayerData> getPlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.getData(id);
    }

    @Override
    public @NotNull CompletableFuture<IPlayerData> requirePlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.requireData(id);
    }

    @Override
    public @Nullable UpgradeLevel getNextThroughputUpgrade(int currentThroughput) {
        if (throughputUpgrades == null)
            throw new UnsupportedOperationException("API is not ready yet");
        for (Map.Entry<Integer, UpgradeLevel> entry : throughputUpgrades.entrySet()) {
            if (entry.getKey() > currentThroughput) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public @Nullable UpgradeLevel getNextCapacityUpgrade(int currentCapacity) {
        if (capacityUpgrade == null)
            throw new UnsupportedOperationException("API is not ready yet");
        for (Map.Entry<Integer, UpgradeLevel> entry : capacityUpgrade.entrySet()) {
            if (entry.getKey() > currentCapacity) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public ShopProviderType getShopProvider() {
        return plugin.mainConfig.shopProvider;
    }
}
