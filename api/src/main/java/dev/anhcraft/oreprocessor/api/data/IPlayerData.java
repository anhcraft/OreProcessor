package dev.anhcraft.oreprocessor.api.data;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPlayerData extends ModifiableData {
    int getDataVersion();

    boolean hasHideTutorial();

    @ApiStatus.Internal
    void setHideTutorial(boolean value);

    @NotNull
    List<String> listOreIds();

    @Nullable
    IOreData getOreData(@NotNull String ore);

    @NotNull
    IOreData requireOreData(@NotNull String ore);

    long getHibernationStart();

    @ApiStatus.Internal
    void setHibernationStart(long hibernationStart);
}
