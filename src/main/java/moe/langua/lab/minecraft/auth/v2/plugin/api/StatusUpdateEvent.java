package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;

import java.util.UUID;

public interface StatusUpdateEvent {
    void onPlayerStatusUpdate(UUID uniqueID, PlayerStatus playerStatus);
}
