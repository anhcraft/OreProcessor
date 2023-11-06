package dev.anhcraft.oreprocessor.storage.player.flatfile.converter;

import dev.anhcraft.oreprocessor.storage.player.flatfile.model.GenericPlayerDataConfig;
import dev.anhcraft.oreprocessor.storage.player.flatfile.model.PlayerDataConfigV0;
import dev.anhcraft.oreprocessor.storage.player.flatfile.model.PlayerDataConfigV1;

public class PlayerDataConverter {
    public static GenericPlayerDataConfig convert(PlayerDataConfigV0 config) {
        PlayerDataConfigV1 newData = new PlayerDataConfigV1();
        newData.hideTutorial = config.hideTutorial;
        newData.hibernationStart = config.hibernationStart;
        newData.dataVersion = 1;
        return newData;
    }
}
