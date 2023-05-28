package pukpukpuk.uskfeatures;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TabListTextController implements Listener {

    private double previousTPS = -1;
    private final HashMap<String, Integer> previousPings = new LinkedHashMap<>();

    public TabListTextController(USKFeatures uskFeatures) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(uskFeatures, () -> {
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

        Component footer = Component.newline()
                .append(getTPSLine())
                .appendSpace()
                .appendSpace()
                .append(getPingLine(player))
                .appendNewline();

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private Component getTPSLine() {
        double currentTPS = getTPS();

        ValueState state = currentTPS > previousTPS ? ValueState.GOOD_INCREASED
                : currentTPS < previousTPS ? ValueState.BAD_DECREASED
                : ValueState.NO_CHANGE;

        return ColorTable.text(" TPS: ")
                .append(ColorTable.HIGHLIGHTED.coloredText(currentTPS))
                .appendSpace()
                .append(state.symbol)
                .appendSpace();
    }

    private Component getPingLine(Player player) {
        int previous = previousPings.getOrDefault(player.getName(), -1);
        int current = player.getPing();

        ValueState state = current < previous ? ValueState.GOOD_DECREASED
                : current > previous ? ValueState.BAD_INCREASED
                : ValueState.NO_CHANGE;

        return ColorTable.text(" Пинг: ")
                .append(ColorTable.HIGHLIGHTED.coloredText(player.getPing()))
                .appendSpace()
                .append(state.symbol)
                .appendSpace();
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
        return Math.min(Math.floor(Bukkit.getTPS()[0]*10d)/10, 20);
    }
}
