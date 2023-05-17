package dev.anhcraft.oreprocessor.integration.shop;

import dev.anhcraft.oreprocessor.integration.Integration;
import org.bukkit.Material;

public interface ShopProvider extends Integration {
    boolean canSell(Material material);

    double getSellPrice(Material material, int amount);
}
