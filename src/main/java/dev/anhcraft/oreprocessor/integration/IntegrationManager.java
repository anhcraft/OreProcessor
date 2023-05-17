package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.config.utils.ObjectUtil;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.integration.shop.ShopGuiPlusBridge;
import dev.anhcraft.oreprocessor.integration.shop.ShopProvider;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IntegrationManager {
    private final Map<String, Integration> integrationMap = new HashMap<>();
    private final OreProcessor plugin;

    public IntegrationManager(OreProcessor plugin) {
        this.plugin = plugin;

        tryHook("AureliumSkills", AureliumSkillsBridge.class);
        tryHook("ShopGUIPlus", ShopGuiPlusBridge.class);
    }

    private void tryHook(String plugin, Class<? extends Integration> clazz) {
        if (this.plugin.getServer().getPluginManager().isPluginEnabled(plugin)) {
            Object instance = null;
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 1) {
                    try {
                        instance = constructor.newInstance(plugin);
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
            this.plugin.getLogger().info("[Integration] Hooked to " + plugin);
        }
    }

    public Integration getIntegration(String plugin) {
        return integrationMap.get(plugin);
    }

    public Optional<ShopProvider> getShopProvider(String plugin) {
        return Optional.ofNullable(integrationMap.get(plugin))
                .filter(i -> i instanceof ShopProvider)
                .map(i -> (ShopProvider) i);
    }
}
