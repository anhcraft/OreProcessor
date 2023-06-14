package dev.anhcraft.oreprocessor.storage.server;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.oreprocessor.OreProcessor;

import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
public class ServerDataConfig {
    @Exclude
    public AtomicBoolean dirty = new AtomicBoolean(false);

    public int dataVersion = OreProcessor.LATEST_SERVER_DATA_VERSION;
}
