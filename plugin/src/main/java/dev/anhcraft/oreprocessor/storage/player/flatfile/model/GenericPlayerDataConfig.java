package dev.anhcraft.oreprocessor.storage.player.flatfile.model;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.oreprocessor.OreProcessor;

@Configurable
public class GenericPlayerDataConfig {
    public int dataVersion = OreProcessor.LATEST_PLAYER_DATA_VERSION;
}
