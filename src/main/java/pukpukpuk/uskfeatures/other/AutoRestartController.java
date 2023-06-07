package pukpukpuk.uskfeatures.other;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.ComponentUtils;
import pukpukpuk.uskfeatures.Controller;
import pukpukpuk.uskfeatures.USKFeatures;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Controller
public class AutoRestartController implements Listener {

    public AutoRestartController() {
        USKFeatures.getCommandManager().registerCommand(new UptimeCommand());
    }

    @CommandAlias("uptime")
    public static class UptimeCommand extends BaseCommand {
        @Default
        public void OnDefault(CommandSender player) {
            Component hint = ComponentUtils.formatColors("<h>Аптайм</h>" +
                    "\nВремя с момента запуска сервера");

            Component component = ComponentUtils.format(
                    ComponentUtils.formatColors(
                            String.format(" <h>⌚</h> @0 <h>%s</h>", getUptime())
                    ),
                    ColorTable.text("Аптайм сервера:").hoverEvent(HoverEvent.showText(hint))
            );

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
