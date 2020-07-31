package moe.langua.lab.minecraft.auth.v2.client.api;

import moe.langua.lab.minecraft.auth.v2.client.api.exception.NotInitializedException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpigotListeners implements Listener {
    private final MelonAuthV2API instance;

    public SpigotListeners(MelonAuthV2API instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED))
            instance.loginPlayer(event.getUniqueId());
        try {
            Bukkit.getLogger().info("Status of uuid " + event.getUniqueId().toString() + " is " + MelonAuthV2API.getStatus((event.getUniqueId())));
        } catch (NotInitializedException ignore) {
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        instance.logoutPlayer(event.getPlayer().getUniqueId());
    }

}
