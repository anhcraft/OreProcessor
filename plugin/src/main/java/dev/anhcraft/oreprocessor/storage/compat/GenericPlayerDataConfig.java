package dev.anhcraft.oreprocessor.storage.compat;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.oreprocessor.OreProcessor;

@Configurable
public class GenericPlayerDataConfig {
    public int dataVersion = OreProcessor.LATEST_PLAYER_DATA_VERSION;
}
