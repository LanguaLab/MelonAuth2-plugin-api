package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;

import java.util.UUID;

/**
 * Called when a player login to the server
 * <p>
 * {@code playerStatus} may be {@code null} if the network error occurred when a player is logging in.
 * <p>
 * use {@link MelonAuth2API#registerLoginEvent(LoginEvent)} to register a LoginEvent.
 */
public interface LoginEvent {
    void login(UUID uniqueID, PlayerStatus playerStatus, LoginResult loginResult);

}
