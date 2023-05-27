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
        }, 0, 10);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        updatePlayerListTexts(event.getPlayer());
    }

    private void updatePlayerListTexts(Player player) {
        Component header = Component.text(" \nUSK 5");
        header = header.color(ColorTable.HIGHLIGHTED.getColor()).decorate(TextDecoration.BOLD);

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
        //String footerText = String.format(" \n TPS: %s \n Пинг: %s \n ", getTPS(), player.getPing());

        Component tpsLine = Component.text(" TPS: ").color(ColorTable.DEFAULT.getColor());
        tpsLine = tpsLine.append(Component.text(getTPS()).color(ColorTable.HIGHLIGHTED.getColor()))
                .appendSpace()
                .append(state.symbol)
                .appendSpace();

        return tpsLine;
    }

    private Component getPingLine(Player player) {
        int previous = previousPings.getOrDefault(player.getName(), -1);
        int current = player.getPing();

        ValueState state = current < previous ? ValueState.GOOD_DECREASED
                : current > previous ? ValueState.BAD_INCREASED
                : ValueState.NO_CHANGE;

        Component pingLine = Component.text(" Пинг: ").color(ColorTable.DEFAULT.getColor());
        pingLine = pingLine.append(Component.text(player.getPing()).color(ColorTable.HIGHLIGHTED.getColor()))
                .appendSpace()
                .append(state.symbol)
                .appendSpace();

        return pingLine;
    }

    @AllArgsConstructor
    private enum ValueState {
        NO_CHANGE(Component.text("-").color(ColorTable.DEFAULT.getColor())),
        GOOD_INCREASED(Component.text("↑").color(ColorTable.SUCCESS.getColor())),
        GOOD_DECREASED(Component.text("↓").color(ColorTable.SUCCESS.getColor())),
        BAD_INCREASED(Component.text("↑").color(ColorTable.ERROR.getColor())),
        BAD_DECREASED(Component.text("↓").color(ColorTable.ERROR.getColor())),
        ;

        private final Component symbol;
    }

    private double getTPS() {
        return Math.floor(Bukkit.getTPS()[0]*10d)/10;
    }
}
