package dev.anhcraft.oreprocessor.api.data;

import dev.anhcraft.oreprocessor.api.data.stats.Monitored;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PlayerData extends Modifiable, Monitored, Versioned {
    int getDataVersion();

    boolean isTutorialHidden();

    @ApiStatus.Internal
    void hideTutorial(boolean value);

    @NotNull
    List<String> listOreIds();

    @Nullable
    OreData getOreData(@NotNull String ore);

    @NotNull
    OreData requireOreData(@NotNull String ore);

    long getHibernationStart();

    @ApiStatus.Internal
    void setHibernationStart(long hibernationStart);
}
