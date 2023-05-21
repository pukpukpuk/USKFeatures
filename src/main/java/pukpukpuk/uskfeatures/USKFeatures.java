package pukpukpuk.uskfeatures;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class USKFeatures extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new NamesColorController(), this);
        // сделать цветоники
        // чат
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
