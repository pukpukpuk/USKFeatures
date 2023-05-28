package pukpukpuk.uskfeatures;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import pukpukpuk.uskfeatures.controllers.ChatController;
import pukpukpuk.uskfeatures.controllers.NamesColorController;
import pukpukpuk.uskfeatures.controllers.PingCommandController;
import pukpukpuk.uskfeatures.controllers.TabListTextController;

import java.util.HashSet;
import java.util.Set;

public final class USKFeatures extends JavaPlugin {

    @Getter
    private static PaperCommandManager commandManager;

    @Getter
    private static USKFeatures plugin;

    @Override
    public void onEnable() {
        plugin = this;

        commandManager = new PaperCommandManager(this);
        registerComponents();
    }

    private void registerComponents() {
        String pkg = USKFeatures.class.getPackage().getName();

        Reflections reflections = new Reflections(pkg);
        Set<Class<?>> classes = new HashSet<>(reflections.getSubTypesOf(Listener.class));

        for (Class<?> cls : classes) {
            try {
                Class<Listener> module = (Class<Listener>) cls;
                Listener listener = module.getDeclaredConstructor().newInstance();

                Bukkit.getPluginManager().registerEvents(listener, this);
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().warning(e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() { }
}
