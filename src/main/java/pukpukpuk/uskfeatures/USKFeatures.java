package pukpukpuk.uskfeatures;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import pukpukpuk.uskfeatures.controllers.IController;

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
        String packageName = IController.class.getPackage().getName();

        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = new HashSet<>(reflections.getSubTypesOf(IController.class));

        for (Class<?> cls : classes) {
            try {
                Class<IController> module = (Class<IController>) cls;
                IController controller = module.getDeclaredConstructor().newInstance();

                Bukkit.getPluginManager().registerEvents(controller, this);
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().warning(e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
    }
}
