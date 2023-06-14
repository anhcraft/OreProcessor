package dev.anhcraft.oreprocessor.storage.server;

import dev.anhcraft.oreprocessor.api.data.ServerData;

public class ServerDataImpl implements ServerData {
    private final ServerDataConfig config;

    public ServerDataImpl(ServerDataConfig config) {
        this.config = config;
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }

    @Override
    public int getDataVersion() {
        return config.dataVersion;
    }
}
