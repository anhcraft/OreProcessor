package dev.anhcraft.oreprocessor.api.data.stats;

import dev.anhcraft.oreprocessor.api.data.Modifiable;

public interface Statistics extends Modifiable {
    /**
     * Gets the mining count of an or multiple ore(s).<br>
     * <ul>
     *   <li>Get one: <code>iron</code></li>
     *   <li>Get multiple: <code>iron,gold,diamond</code></li>
     *   <li>Get all: <code>*</code></li>
     * </ul>
     * @param oreQuery The query to select ores for counting
     * @return The mining count
     */
    long getMiningCount(String oreQuery);

    void setMiningCount(String ore, long amount);

    void addMiningCount(String ore, long amount);

    /**
     * Gets the feedstock count of an or multiple ore(s).
     * @param oreQuery The query to select ores for counting
     * @see #getMiningCount(String)
     * @return The feedstock count
     */
    long getFeedstockCount(String oreQuery);

    void setFeedstockCount(String ore, long amount);

    void addFeedstockCount(String ore, long amount);

    /**
     * Gets the product count of an or multiple ore(s).
     * @param oreQuery The query to select ores for counting
     * @see #getMiningCount(String)
     * @return The product count
     */
    long getProductCount(String oreQuery);

    void setProductCount(String ore, long amount);

    void addProductCount(String ore, long amount);
}
