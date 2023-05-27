package pukpukpuk.uskfeatures;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class USKFeatures extends JavaPlugin {

    @Getter
    private static PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new PaperCommandManager(this);

        getServer().getPluginManager().registerEvents(new NamesColorController(), this);
        getServer().getPluginManager().registerEvents(new ChatController(), this);
        getServer().getPluginManager().registerEvents(new PingCommandController(), this);

    }

    @Override
    public void onDisable() { }
}
