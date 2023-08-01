package dev.anhcraft.oreprocessor.api;

import org.jetbrains.annotations.NotNull;

public class ApiProvider {
    private static OreProcessorApi api;

    @NotNull
    public static OreProcessorApi getApi() {
        if (api == null) {
            throw new IllegalStateException("Api is not initialized");
        }
        return api;
    }
}
