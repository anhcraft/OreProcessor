package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlaceholderApiBridge extends PlaceholderExpansion implements Integration {
    private static final String NAME = "name_";
    private static final String CAPACITY = "capacity_";
    private static final String THROUGHPUT = "throughput_";
    private static final String THROUGHPUT_PER_MINUTE = "throughput_per_minute_";
    private static final String FEEDSTOCK = "feedstock_";
    private static final String PRODUCTS = "products_";
    private static final String FREE_SPACE = "free_space_";
    private static final String SERVER_STATS_MINING = "server_stats_mining_";
    private static final String SERVER_STATS_FEEDSTOCK = "server_stats_feedstock_";
    private static final String SERVER_STATS_PRODUCTS = "server_stats_products_";
    private static final String PLAYER_STATS_MINING = "player_stats_mining_";
    private static final String PLAYER_STATS_FEEDSTOCK = "player_stats_feedstock_";
    private static final String PLAYER_STATS_PRODUCTS = "player_stats_products_";
    private final OreProcessor plugin;

    public PlaceholderApiBridge(OreProcessor plugin) {
        this.plugin = plugin;
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ore";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        // ore_name_<ore>
        if(params.startsWith(NAME)){
            Ore ore = OreProcessor.getApi().getOre(params.substring(NAME.length()));
            if (ore == null) return null;
            return ore.getName();
        }

        // ore_server_stats_mining_<ore-query>
        else if(params.startsWith(SERVER_STATS_MINING)){
            String query = params.substring(SERVER_STATS_MINING.length());
            long count = OreProcessor.getApi().getServerData().getCumulativeStats().getMiningCount(query);
            return Long.toString(count);
        }

        // ore_server_stats_feedstock_<ore-query>
        else if(params.startsWith(SERVER_STATS_FEEDSTOCK)){
            String query = params.substring(SERVER_STATS_FEEDSTOCK.length());
            long count = OreProcessor.getApi().getServerData().getCumulativeStats().getFeedstockCount(query);
            return Long.toString(count);
        }

        // ore_server_stats_products_<ore-query>
        else if(params.startsWith(SERVER_STATS_PRODUCTS)){
            String query = params.substring(SERVER_STATS_PRODUCTS.length());
            long count = OreProcessor.getApi().getServerData().getCumulativeStats().getProductCount(query);
            return Long.toString(count);
        }

        else if (player != null) {
            Optional<PlayerData> pd = OreProcessor.getApi().getPlayerData(player.getUniqueId());
            if (!pd.isPresent()) return null;

            // ore_capacity_<ore>
            if (params.startsWith(CAPACITY)) {
                String ore = params.substring(CAPACITY.length());
                OreData oreData = pd.get().getOreData(ore);
                if (oreData == null) return null;
                return Integer.toString(oreData.getCapacity());
            }
            // ore_throughput_per_minute_<ore>
            else if (params.startsWith(THROUGHPUT_PER_MINUTE)) {
                String ore = params.substring(THROUGHPUT_PER_MINUTE.length());
                OreData oreData = pd.get().getOreData(ore);
                if (oreData == null) return null;
                int tpm = OreProcessor.getApi().getThroughputPerMinute(oreData.getThroughput());
                return Integer.toString(tpm);
            }
            // ore_throughput_<ore>
            else if (params.startsWith(THROUGHPUT)) {
                String ore = params.substring(THROUGHPUT.length());
                OreData oreData = pd.get().getOreData(ore);
                if (oreData == null) return null;
                return Integer.toString(oreData.getThroughput());
            }
            // ore_feedstock_<ore>
            else if (params.startsWith(FEEDSTOCK)) {
                String ore = params.substring(FEEDSTOCK.length());
                OreData oreData = pd.get().getOreData(ore);
                if (oreData == null) return null;
                return Integer.toString(oreData.countAllFeedstock());
            }
            // ore_products_<ore>
            else if (params.startsWith(PRODUCTS)) {
                String ore = params.substring(PRODUCTS.length());
                OreData oreData = pd.get().getOreData(ore);
                if (oreData == null) return null;
                return Integer.toString(oreData.countAllProducts());
            }
            // ore_free_space_<ore>
            else if (params.startsWith(FREE_SPACE)) {
                String ore = params.substring(FREE_SPACE.length());
                OreData oreData = pd.get().getOreData(ore);
                if (oreData == null) return null;
                return Integer.toString(oreData.getFreeSpace());
            }
            // ore_player_stats_mining_<ore-query>
            else if(params.startsWith(PLAYER_STATS_MINING)){
                String query = params.substring(PLAYER_STATS_MINING.length());
                long count = pd.get().getCumulativeStats().getMiningCount(query);
                return Long.toString(count);
            }
            // ore_player_stats_feedstock_<ore-query>
            else if(params.startsWith(PLAYER_STATS_FEEDSTOCK)){
                String query = params.substring(PLAYER_STATS_FEEDSTOCK.length());
                long count = pd.get().getCumulativeStats().getFeedstockCount(query);
                return Long.toString(count);
            }
            // ore_player_stats_products_<ore-query>
            else if(params.startsWith(PLAYER_STATS_PRODUCTS)){
                String query = params.substring(PLAYER_STATS_PRODUCTS.length());
                long count = pd.get().getCumulativeStats().getProductCount(query);
                return Long.toString(count);
            }
        }

        return null;
    }
}
