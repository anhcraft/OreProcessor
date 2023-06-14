package dev.anhcraft.oreprocessor.api.data;

import dev.anhcraft.oreprocessor.api.data.stats.TrackedData;

public interface ServerData extends ModifiableData, TrackedData {
    int getDataVersion();
}
