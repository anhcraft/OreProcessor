package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Optional;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class PickupTrackerSettings {
    public boolean enabled;
    @Optional
    public String message = "&a+{amount} &f{item}";
    public float interval = 1f;
}
