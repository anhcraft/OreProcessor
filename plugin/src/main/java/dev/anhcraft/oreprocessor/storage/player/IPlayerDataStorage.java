package dev.anhcraft.oreprocessor.storage.player;

import dev.anhcraft.oreprocessor.api.data.PlayerData;

import java.util.UUID;

public interface IPlayerDataStorage {
    PlayerData loadOrCreate(UUID id);
}
