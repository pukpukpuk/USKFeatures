package pukpukpuk.uskfeatures.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.ComponentUtils;
import pukpukpuk.uskfeatures.Controller;
import pukpukpuk.uskfeatures.USKFeatures;

@Controller
@CommandAlias("ping")
public class PingCommand extends BaseCommand implements Listener {

    public PingCommand() {
        USKFeatures.getCommandManager().registerCommand(this);
    }

    @CatchUnknown
    @CommandCompletion("@players")
    public void OnDefault(Player recipient, @Optional @Single String name) {
        Player player = Bukkit.getPlayer(name == null ? recipient.getName() : name);
        printPing(recipient, player == null ? recipient : player);
    }

    private void printPing(Player recipient, Player player) {
        Component hint = ComponentUtils.formatColors("<h>Пинг</h>" +
                "\nВремя, за которое отправленные данные достигают сервера.");

        Component text = ColorTable.text(recipient == player ? "Твой пинг:"
                        : String.format("Пинг игрока %s:", player.getName()))
                .hoverEvent(HoverEvent.showText(hint));

        Component component = ComponentUtils.format(
                ComponentUtils.formatColors(" <h>⇵</h> @0 @1"),
                text,
                ColorTable.HIGHLIGHTED.coloredText(player.getPing())
        );

        recipient.sendMessage(component);
    }
}
