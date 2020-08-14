package moe.langua.lab.minecraft.auth.v2.plugin.api;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;

public class BungeeCordLoader extends Plugin implements Listener {
    private MelonAuth2API instance;

    @Override
    public void onEnable() {
        if (!getDataFolder().mkdir() && getDataFolder().isFile())
            throw new IllegalArgumentException(getDataFolder().getAbsolutePath() + " should be a directory, but found a file.");
        try {
            instance = new MelonAuth2API(new File(getDataFolder(), "config.json"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onPlayerPreLogin(PreLoginEvent event) {
        event.registerIntent(this);
        this.getProxy().getScheduler().runAsync(this, () -> {
            LoginResult result = instance.loginPlayer(event.getConnection().getUniqueId());
            if (!result.isAllowLogin()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText(result.getKickedMessage()));
            }
            event.completeIntent(this);
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        instance.logOutPlayer(event.getPlayer().getUniqueId());
    }
}
