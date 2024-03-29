package dev.anhcraft.oreprocessor.storage.player;

import dev.anhcraft.oreprocessor.storage.player.compat.GenericPlayerDataConfig;
import dev.anhcraft.oreprocessor.storage.player.compat.PlayerDataConfigV0;

public class PlayerDataConverter {
    public static GenericPlayerDataConfig convert(PlayerDataConfigV0 config) {
        PlayerDataConfigV1 newData = new PlayerDataConfigV1();
        newData.hideTutorial = config.hideTutorial;
        newData.hibernationStart = config.hibernationStart;
        newData.dataVersion = 1;
        return newData;
    }
}
