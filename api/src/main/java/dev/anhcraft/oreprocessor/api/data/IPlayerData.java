package dev.anhcraft.oreprocessor.api.data;

import org.jetbrains.annotations.Nullable;

public interface IPlayerData extends ModifiableData {
    int hasHideTutorial();

    @Nullable
    IOreData getOreData(String ore);
}
