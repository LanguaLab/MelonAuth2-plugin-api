package moe.langua.lab.minecraft.auth.v2.client.api;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SpigotLoader extends JavaPlugin {
    MelonAuthV2API instance;
    @Override
    public void onEnable() {
        try {
             instance = new MelonAuthV2API(this.getDataFolder(),this.getLogger());
        } catch (IOException e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getServer().getPluginManager().registerEvents(new SpigotListeners(instance),this);
    }

    @Override
    public void onDisable() {
        if(instance==null) return;
        try {
            instance.saveCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
