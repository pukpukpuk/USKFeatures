package pukpukpuk.uskfeatures;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@CommandAlias("ping")
public class PingCommandController extends BaseCommand implements Listener {

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

        recipient.sendMessage(ColorTable.HIGHLIGHTED.coloredText(" ⌛")
                .append(ColorTable.text(text))
                .append(ColorTable.HIGHLIGHTED.coloredText(player.getPing()))
        );
    }
}
