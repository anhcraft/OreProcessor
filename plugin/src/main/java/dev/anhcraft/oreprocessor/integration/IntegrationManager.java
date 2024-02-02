package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.config.utils.ObjectUtil;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.integration.adder.ItemCustomizer;
import dev.anhcraft.oreprocessor.integration.adder.OraxenBridge;
import dev.anhcraft.oreprocessor.integration.shop.EconomyShopGUIBridge;
import dev.anhcraft.oreprocessor.integration.shop.ShopGuiPlusBridge;
import dev.anhcraft.oreprocessor.integration.shop.ShopProvider;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntegrationManager {
    private final Map<String, Integration> integrationMap = new HashMap<>();
    private final Map<MaterialClass, ItemCustomizer> itemCustomizers = new EnumMap<>(MaterialClass.class);
    private final OreProcessor mainPlugin;

    public IntegrationManager(OreProcessor mainPlugin) {
        this.mainPlugin = mainPlugin;

        tryHook("AureliumSkills", AureliumSkillsBridge.class);
        tryHook("ShopGUIPlus", ShopGuiPlusBridge.class);
        tryHook("EconomyShopGUI", EconomyShopGUIBridge.class);
        tryHook("EconomyShopGUI-Premium", EconomyShopGUIBridge.class);
        tryHook("PlaceholderAPI", PlaceholderApiBridge.class);
        tryHook("eco", EcoBridge.class);
        tryHook("AdvancedEnchantments", AdvancedEnchantmentBridge.class);
        tryHook("Oraxen", OraxenBridge.class);
    }

    private void tryHook(String plugin, Class<? extends Integration> clazz) {
        if (this.mainPlugin.getServer().getPluginManager().isPluginEnabled(plugin)) {
            Object instance = null;
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 1) {
                    try {
                        instance = constructor.newInstance(this.mainPlugin);
                        break;
                    } catch (InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (instance == null) {
                try {
                    instance = ObjectUtil.newInstance(clazz);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
            integrationMap.put(plugin, (Integration) instance);
            if (instance instanceof ItemCustomizer) {
                itemCustomizers.put(((ItemCustomizer) instance).getMaterialClass(), (ItemCustomizer) instance);
            }
            this.mainPlugin.getLogger().info("[Integration] Hooked to " + plugin);
        }
    }

    public Integration getIntegration(String plugin) {
        return integrationMap.get(plugin);
    }

    public Stream<Integration> streamIntegration() {
        return integrationMap.values().stream();
    }

    public Collection<ItemCustomizer> getItemCustomizers() {
        return itemCustomizers.values();
    }

    public Optional<ShopProvider> getShopProvider(@Nullable ShopProviderType shopProviderType) {
        if (shopProviderType == null) return Optional.empty();
        return Optional.ofNullable(integrationMap.get(shopProviderType.getPlugin()))
                .filter(i -> i instanceof ShopProvider)
                .map(i -> (ShopProvider) i);
    }

    public ItemCustomizer getItemCustomizer(MaterialClass materialClass) {
        return itemCustomizers.get(materialClass);
    }

    public Set<String> getAllMaterials() {
        Set<String> result = new HashSet<>();
        for (ItemCustomizer integration : itemCustomizers.values()) {
            result.addAll(integration.getCustomMaterials().stream().map(UMaterial::toString).collect(Collectors.toList()));
        }
        return result;
    }
}
