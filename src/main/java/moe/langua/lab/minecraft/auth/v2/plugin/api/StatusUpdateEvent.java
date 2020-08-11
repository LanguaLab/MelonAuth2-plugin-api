package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;

import java.util.UUID;

public abstract class StatusUpdateEvent {
    abstract void onPlayerStatusUpdate(UUID uniqueID, PlayerStatus playerStatus);
}
