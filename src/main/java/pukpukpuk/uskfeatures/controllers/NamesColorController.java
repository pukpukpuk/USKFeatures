package pukpukpuk.uskfeatures.controllers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pukpukpuk.uskfeatures.ColorTable;

public class NamesColorController implements IController {

    public static final MiniMessage COLORS = MiniMessage.builder().tags(TagResolver.builder()
            .resolver(StandardTags.color())
            .build()).build();

    public NamesColorController() {
        Bukkit.getOnlinePlayers().forEach(this::updatePlayerNameColor);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        updatePlayerNameColor(event.getPlayer());
    }

    @EventHandler
    public void OnChangedWorld(PlayerChangedWorldEvent event) {
        updatePlayerNameColor(event.getPlayer());
    }

    private void updatePlayerNameColor(Player player) {
        Component component = Component.text(player.getName());

        String environment = getEnvironmentName(player);
        component = component.color(ColorTable.tryGet(environment + "_DIMENSION").getColor());

        player.playerListName(component);
        player.displayName(component);
    }

    public static String getEnvironmentName(Player player) {
        return player.getWorld().getEnvironment().name().toUpperCase();
    }
}
