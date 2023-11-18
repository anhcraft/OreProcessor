package dev.anhcraft.oreprocessor.api.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface OreData extends Modifiable {
    int getThroughput();

    void setThroughput(int amount);

    void addThroughput(int amount);

    int getCapacity();

    void setCapacity(int amount);

    void addCapacity(int amount);

    @NotNull
    Set<Material> getFeedstock();

    void addFeedstock(@NotNull Material material, int amount);

    int countFeedstock(@NotNull Material material);

    int countAllFeedstock();

    @NotNull
    Set<Material> getProducts();

    /**
     * Adds an amount of products.
     * @param material the product material
     * @param expectedAmount the expected amount to be added
     * @param force whether to force the addition
     * @return the actual amount being added
     */
    int addProduct(@NotNull Material material, int expectedAmount, boolean force);

    /**
     * Takes an amount of products.
     * @param material the product material
     * @param expectedAmount the expected amount to be taken
     * @return the actual amount being taken
     */
    int takeProduct(@NotNull Material material, int expectedAmount);

    /**
     * Set an amount of products.
     * @param material the product material
     * @param expectedAmount the expected amount
     * @param force whether to force the addition
     * @return the actual amount
     */
    int setProduct(@NotNull Material material, int expectedAmount, boolean force);

    /**
     * Tests and takes products if success.
     * @param material the product material
     * @param expectedAmount the expected amount to be taken
     * @param function the function with input is the actual amount being taken,
     *                 and the output is whether the operation is allowed to continue
     * @return same as the output from the function; {@code true} if the data was changed
     */
    boolean testAndTakeProduct(@NotNull Material material, int expectedAmount, @NotNull Function<Integer, Boolean> function);

    /**
     * Tests and sets products if success.
     * @param material the product material
     * @param function the function with input is the current amount, and output is the new amount
     * @return {@code true} if the data was changed
     */
    boolean testAndSetProduct(@NotNull Material material, @NotNull Function<Integer, Integer> function);

    int countProduct(@NotNull Material material);

    int countAllProducts();

    boolean isFull();

    int getFreeSpace();

    @ApiStatus.Internal
    Map<Material, Integer> process(int throughputMultiplier, @NotNull Function<Material, ItemStack> function);
}
