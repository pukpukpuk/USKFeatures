package pukpukpuk.uskfeatures.controllers;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.USKFeatures;

@CommandAlias("ping")
public class PingCommandController extends BaseCommand implements IController {

    public PingCommandController() {
        USKFeatures.getCommandManager().registerCommand(this);
    }

    @CatchUnknown
    @CommandCompletion("@players")
    public void OnDefault(Player recipient, @Optional @Single String name) {
        Player player = Bukkit.getPlayer(name == null ? recipient.getName() : name);
        printPing(recipient, player == null ? recipient : player);
    }

    private void printPing(Player recipient, Player player) {
        String text = recipient == player ? " Твой пинг: " : String.format(" Пинг игрока %s: ", player.getName());

        Component hint = ColorTable.HIGHLIGHTED.coloredText("Пинг\n")
                .append(ColorTable.text("Время, за которое отправленные данные достигают сервера."));

        recipient.sendMessage(ColorTable.HIGHLIGHTED.coloredText(" ⇵")
                .append(ColorTable.text(text).hoverEvent(HoverEvent.showText(hint)))
                .append(ColorTable.HIGHLIGHTED.coloredText(player.getPing()))
        );
    }
}
