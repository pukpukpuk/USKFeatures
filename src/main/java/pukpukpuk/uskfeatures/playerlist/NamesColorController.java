package pukpukpuk.uskfeatures.playerlist;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.Controller;

@Controller
public class NamesColorController implements Listener {

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
        World.Environment environment = player.getWorld().getEnvironment();
        ColorTable color = getColor(environment);

        Component component = color.coloredText(player.getName());

        player.playerListName(component);
        player.displayName(component);
    }

    private ColorTable getColor(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> ColorTable.NORMAL_DIMENSION;
            case NETHER -> ColorTable.NETHER_DIMENSION;
            case THE_END -> ColorTable.THE_END_DIMENSION;
            default -> ColorTable.HIGHLIGHTED;
        };
    }
}
