package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.Map;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class AccessibilitySettings {

    public Map<ClickType, Double> quickSellRatio = new HashMap<>();

    public Map<ClickType, Integer> takeAmount = new HashMap<>();
}
