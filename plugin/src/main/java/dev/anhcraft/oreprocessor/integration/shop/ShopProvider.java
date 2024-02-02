package dev.anhcraft.oreprocessor.integration.shop;

import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.integration.Integration;

public interface ShopProvider extends Integration {
    boolean canSell(UMaterial material);

    double getSellPrice(UMaterial material, int amount);
}
