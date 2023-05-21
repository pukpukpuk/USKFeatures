package pukpukpuk.uskfeatures;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class NamesColorController implements Listener {

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

        String environment = player.getWorld().getEnvironment().name().toUpperCase();
        component = component.color(TextColor.color(ColorTable.tryGet(environment).getColor()));

        player.playerListName(component);
    }
}
