package dev.anhcraft.oreprocessor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreProcessorApi;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.data.ServerData;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import dev.anhcraft.oreprocessor.api.upgrade.UpgradeLevel;
import dev.anhcraft.oreprocessor.api.util.WheelSelection;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevelConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class OreProcessorApiImpl implements OreProcessorApi {
    private final OreProcessor plugin;
    private Map<String, Ore> ores;
    private Map<Material, Ore> block2ores;
    private Multimap<Material, Ore> feedstock2ores;
    private Multimap<Material, Ore> itemStorage2ores;
    private TreeMap<Integer, UpgradeLevel> throughputUpgrades;
    private TreeMap<Integer, UpgradeLevel> capacityUpgrade;

    public OreProcessorApiImpl(OreProcessor plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        Map<String, Ore> oreMap = new LinkedHashMap<>();
        feedstock2ores = HashMultimap.create();

        for (Map.Entry<String, OreConfig> entry : plugin.mainConfig.ores.entrySet()) {
            OreConfig oreConfig = entry.getValue();

            LinkedHashMap<String, OreTransform> transformMap = new LinkedHashMap<>();

            for (Map.Entry<String, Map<Material, WheelSelection<ItemStack>>> e : oreConfig.transform.entrySet()) {
                transformMap.put(e.getKey(), new OreTransform(e.getKey(), Collections.unmodifiableMap(e.getValue())));
            }

            Set<Material> referenceFeedstock = null;

            for (Iterator<OreTransform> it = transformMap.values().iterator(); it.hasNext(); ) {
                OreTransform value = it.next();

                if (referenceFeedstock == null)
                    referenceFeedstock = value.getFeedstock();
                else if (!value.getFeedstock().equals(referenceFeedstock)) {
                    plugin.getLogger().warning(String.format("Ore '%s' has inconsistent feedstock", entry.getKey()));
                    it.remove();
                }
            }

            if (referenceFeedstock == null || transformMap.isEmpty()) {
                plugin.getLogger().warning(String.format("Ore '%s' has no transform config", entry.getKey()));
                continue;
            }

            if (!transformMap.containsKey("default")) {
                plugin.getLogger().warning(String.format("Ore '%s' has no default transform config", entry.getKey()));
                continue;
            }

            Ore ore = new Ore(
                    entry.getKey(),
                    oreConfig.name,
                    oreConfig.icon,
                    Collections.unmodifiableSet(oreConfig.blocks),
                    Collections.unmodifiableMap(transformMap),
                    Collections.unmodifiableSet(referenceFeedstock)
            );
            oreMap.put(entry.getKey(), ore);

            for (Material material : referenceFeedstock) {
                feedstock2ores.put(material, ore);
            }
        }

        ores = Collections.unmodifiableMap(oreMap);
        feedstock2ores = ImmutableMultimap.copyOf(feedstock2ores);

        throughputUpgrades = new TreeMap<>();
        int lastUpgradeAmount = -1;
        for (UpgradeLevelConfig upgradeLevelConfig : plugin.upgradeConfig.throughputUpgrade.values()) {
            if (lastUpgradeAmount != -1 && lastUpgradeAmount >= upgradeLevelConfig.amount) {
                plugin.getLogger().warning(String.format(
                        "Detected invalid throughput upgrade: %d is not higher than previous upgrade %d",
                        upgradeLevelConfig.amount, lastUpgradeAmount
                ));
                continue;
            } else {
                lastUpgradeAmount = upgradeLevelConfig.amount;
            }
            throughputUpgrades.put(upgradeLevelConfig.amount, new UpgradeLevel(upgradeLevelConfig.amount, upgradeLevelConfig.cost));
        }

        capacityUpgrade = new TreeMap<>();
        lastUpgradeAmount = -1;
        for (UpgradeLevelConfig upgradeLevelConfig : plugin.upgradeConfig.capacityUpgrade.values()) {
            if (lastUpgradeAmount != -1 && lastUpgradeAmount >= upgradeLevelConfig.amount) {
                plugin.getLogger().warning(String.format(
                        "Detected invalid capacity upgrade: %d is not higher than previous upgrade %d",
                        upgradeLevelConfig.amount, lastUpgradeAmount
                ));
                continue;
            } else {
                lastUpgradeAmount = upgradeLevelConfig.amount;
            }
            capacityUpgrade.put(upgradeLevelConfig.amount, new UpgradeLevel(upgradeLevelConfig.amount, upgradeLevelConfig.cost));
        }

        block2ores = new HashMap<>();
        for (Ore ore : oreMap.values()) {
            for (Material block : ore.getBlocks()) {
                if (block2ores.put(block, ore) != null) {
                    plugin.getLogger().warning("Duplication detected: Ore '"+ore.getName()+"' exists in multiple blocks!");
                }
            }
        }

        itemStorage2ores = HashMultimap.create();
        for (Map.Entry<String, List<Material>> entry : plugin.filterConfig.storage.entrySet()) {
            for (Material material : entry.getValue()) {
                if (oreMap.containsKey(entry.getKey()))
                    itemStorage2ores.put(material, oreMap.get(entry.getKey()));
            }
        }
    }

    @Override
    public @NotNull Set<String> getOres() {
        if (ores == null)
            throw new UnsupportedOperationException("API is not ready yet");
        return ores.keySet(); // immutable
    }

    @Override
    public @Nullable Ore getOre(@NotNull String id) {
        if (ores == null)
            throw new UnsupportedOperationException("API is not ready yet");
        return ores.get(id);
    }

    @Override
    public @Nullable Ore getBlockOre(Material block) {
        return block2ores.get(block);
    }

    @Override
    public @NotNull Collection<Ore> getOresAllowFeedstock(Material feedstock) {
        return feedstock2ores.get(feedstock);
    }

    @Override
    public @Nullable Set<Material> getStorageFilter(String id) {
        List<Material> v = plugin.filterConfig.storage.get(id);
        return v == null ? null : EnumSet.copyOf(v);
    }

    @Override
    public @Nullable List<Ore> getStorageAllowItem(Material item) {
        Collection<Ore> v = itemStorage2ores.get(item);
        return v == null ? null : new ArrayList<>(v);
    }

    @Override
    public float getProcessingInterval() {
        return plugin.mainConfig.processingInterval;
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
        return (int) (throughput * 60d / OreProcessor.getApi().getProcessingInterval());
    }

    @Override
    public @NotNull PlayerData getPlayerData(@NotNull Player player) {
        return plugin.playerDataManager.getData(player);
    }

    @Override
    public @NotNull Optional<PlayerData> getPlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.getData(id);
    }

    @Override
    public @NotNull CompletableFuture<PlayerData> requirePlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.requireData(id);
    }

    @Override
    public @NotNull ServerData getServerData() {
        return plugin.serverDataManager.getData();
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
