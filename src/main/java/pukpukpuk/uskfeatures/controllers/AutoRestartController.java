package pukpukpuk.uskfeatures.controllers;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.USKFeatures;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AutoRestartController implements IController {

    public AutoRestartController() {
        USKFeatures.getCommandManager().registerCommand(new UptimeCommand());
    }

    @CommandAlias("uptime")
    public static class UptimeCommand extends BaseCommand {
        @Default
        public void OnDefault(CommandSender player) {
            Component hint = ColorTable.HIGHLIGHTED.coloredText("Аптайм\n")
                    .append(ColorTable.text("Время с момента запуска сервера"));

            Component component = ColorTable.HIGHLIGHTED.coloredText(" ⌚ ")
                    .append(ColorTable.text("Аптайм сервера: ").hoverEvent(HoverEvent.showText(hint)))
                    .append(ColorTable.HIGHLIGHTED.coloredText(getUptime()));

            player.sendMessage(component);
        }

        public static String getUptime() {
            RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.format(new Date(rb.getUptime()));
        }
    }
}
