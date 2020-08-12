package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;

import java.util.UUID;

/**
 * Called when a player login to the server
 * 
 * {@code playerStatus} may be {@code null} if the network error occurred when a player is logging in.
 * 
 * use {@link MelonAuth2API#registerLoginEvent(LoginEvent)} to register a LoginEvent.
 */
public abstract class LoginEvent {
    abstract void login(UUID playerUniqueID, PlayerStatus playerStatus);
}
