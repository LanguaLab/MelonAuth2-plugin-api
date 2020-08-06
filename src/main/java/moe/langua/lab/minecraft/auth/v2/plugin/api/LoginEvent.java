package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;

import java.util.UUID;

public abstract class LoginEvent {
    abstract void login(UUID playerUniqueID, PlayerStatus playerStatus);
}
