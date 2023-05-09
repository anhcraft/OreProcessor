package dev.anhcraft.oreprocessor.util;

import org.bukkit.Material;

public class OreTransform {
    private final Material raw;
    private final Material product;

    public OreTransform(Material raw, Material product) {
        this.raw = raw;
        this.product = product;
    }

    public Material getRaw() {
        return raw;
    }

    public Material getProduct() {
        return product;
    }
}
