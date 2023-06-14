package dev.anhcraft.oreprocessor.api.data.stats;

import dev.anhcraft.oreprocessor.api.data.Modifiable;

public interface Statistics extends Modifiable {
    long getMiningCount(String ore);

    void setMiningCount(String ore, long amount);

    void addMiningCount(String ore, long amount);

    long getFeedstockCount(String ore);

    void setFeedstockCount(String ore, long amount);

    void addFeedstockCount(String ore, long amount);

    long getProductCount(String ore);

    void setProductCount(String ore, long amount);

    void addProductCount(String ore, long amount);
}
