package pukpukpuk.uskfeatures.playerlist;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.ComponentUtils;
import pukpukpuk.uskfeatures.Controller;
import pukpukpuk.uskfeatures.USKFeatures;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
public class TextController implements Listener {

    private double previousTPS = -1;
    private final HashMap<String, Integer> previousPings = new LinkedHashMap<>();

    public TextController() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(USKFeatures.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                updatePlayerListTexts(player);
                previousPings.put(player.getName(), player.getPing());
            });
            previousTPS = getTPS();
        }, 0, 50);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        updatePlayerListTexts(event.getPlayer());
    }

    private void updatePlayerListTexts(Player player) {
        Component header = ColorTable.HIGHLIGHTED.coloredText(" \nUSK 5")
                .decorate(TextDecoration.BOLD);

        Component footer = ComponentUtils.format(" \n @0    @1 \n ",
                getTPSLine(),
                getPingLine(player));

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private Component getTPSLine() {
        double currentTPS = getTPS();

        ValueState state = currentTPS > previousTPS ? ValueState.GOOD_INCREASED
                : currentTPS < previousTPS ? ValueState.BAD_DECREASED
                : ValueState.NO_CHANGE;

        return ComponentUtils.format(
                ComponentUtils.formatColors(
                        String.format("TPS: <h>%s</h> @0", currentTPS)
                ),
                state.symbol
        );
    }

    private Component getPingLine(Player player) {
        int previous = previousPings.getOrDefault(player.getName(), player.getPing());
        int current = player.getPing();

        ValueState state = current < previous ? ValueState.GOOD_DECREASED
                : current > previous ? ValueState.BAD_INCREASED
                : ValueState.NO_CHANGE;

        return ComponentUtils.format(
                ComponentUtils.formatColors(
                        String.format("Пинг: <h>%s</h> @0", player.getPing())
                ),
                state.symbol
        );
    }

    @AllArgsConstructor
    private enum ValueState {
        NO_CHANGE(ColorTable.text("-")),
        GOOD_INCREASED(ColorTable.SUCCESS.coloredText("↑")),
        GOOD_DECREASED(ColorTable.SUCCESS.coloredText("↓")),
        BAD_INCREASED(ColorTable.ERROR.coloredText("↑")),
        BAD_DECREASED(ColorTable.ERROR.coloredText("↓")),
        ;

        private final Component symbol;
    }

    private double getTPS() {
        return Math.min(Math.floor(Bukkit.getTPS()[0] * 10d) / 10, 20);
    }
}
