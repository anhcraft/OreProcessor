package dev.anhcraft.oreprocessor.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPlayerData extends ModifiableData {
    int getDataVersion();

    boolean hasHideTutorial();

    void setHideTutorial(boolean value);

    @NotNull
    List<String> listOreIds();

    @NotNull
    List<IOreData> listOreData();

    @Nullable
    IOreData getOreData(@NotNull String ore);

    @NotNull
    IOreData requireOreData(@NotNull String ore);
}
