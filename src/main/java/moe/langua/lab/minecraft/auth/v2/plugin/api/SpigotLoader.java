package moe.langua.lab.minecraft.auth.v2.plugin.api;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class SpigotLoader extends JavaPlugin implements Listener {
    private MelonAuth2API instance;

    @Override
    public void onEnable() {
        if (!getDataFolder().mkdir() && getDataFolder().isFile())
            throw new IllegalArgumentException(getDataFolder().getAbsolutePath() + " should be a directory, but found a file.");
        try {
            instance = new MelonAuth2API(new File(getDataFolder(), "config.json"));
            instance.checkSettings();
        } catch (IOException e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        LoginResult result = instance.loginPlayer(event.getUniqueId());
        if (!result.isAllowLogin()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(result.getKickedMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        instance.logOutPlayer(event.getPlayer().getUniqueId());
    }
}
