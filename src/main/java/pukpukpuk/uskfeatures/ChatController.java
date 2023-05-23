package pukpukpuk.uskfeatures;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class ChatController implements Listener {

    private final ForceGlobalCommand forceGlobalCommand;

    public ChatController() {
        USKFeatures.getCommandManager().registerCommand(forceGlobalCommand = new ForceGlobalCommand());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        forceGlobalCommand.players.remove(event.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean exclSymbol = message.toCharArray()[0] == '!';
        boolean inPlayers = forceGlobalCommand.players.contains(player.getName());
        boolean toGlobal = exclSymbol || inPlayers;

        if(exclSymbol) {
            message = message.substring(1);
        }

        Component component = createMessageComponent(player, message, toGlobal);

        Bukkit.getConsoleSender().sendMessage(component);
        Bukkit.getOnlinePlayers().forEach(recipient -> {
            boolean sameWorld = player.getWorld() == recipient.getWorld();
            boolean inRadius = sameWorld && player.getLocation().distanceSquared(recipient.getLocation()) <= 128*128;
            boolean canHear = sameWorld && inRadius;

            if(toGlobal || canHear) {
                recipient.sendMessage(component);
            }
        });
    }

    private Component createMessageComponent(Player player, String message, boolean toGlobal) {
        //hover
        //name(hover with time): message
        //Component chatMarkComponent = Component.text(toGlobal ? "Ⓖ " : "Ⓛ ").color(ColorTable.CHAT_MARK.getColor());
        Component chatMarkComponent = toGlobal ?
                Component.text("ɢ ").color(ColorTable.GLOBAL_CHAT_MARK.getColor()) :
                Component.text("ʟ ").color(ColorTable.LOCAL_CHAT_MARK.getColor());

        String hintTitle = toGlobal ? "Глобальный чат\n" : "Локальный чат\n";
        TextColor hintColor = (toGlobal ? ColorTable.GLOBAL_CHAT_MARK : ColorTable.LOCAL_CHAT_MARK).getColor();
        String hintText = toGlobal ? "Это сообщение видят все игроки"
                : "Это сообщение видят только те, кто находится в восьми чанках от вас";

        chatMarkComponent = chatMarkComponent.hoverEvent(
                HoverEvent.showText(
                        Component.text(hintTitle)
                                .color(hintColor)
                                .append(Component.text(hintText)
                                        .color(ColorTable.DEFAULT.getColor()))));

        Component nameComponent = Component.text(player.getName())
                .color((toGlobal ? ColorTable.GLOBAL_CHAT_NAME : ColorTable.LOCAL_CHAT_NAME).getColor())
                .hoverEvent(HoverEvent.showText(Component.text(getTimeText())
                        .color(ColorTable.TIME.getColor())));

        Component messageComponent = Component.text(": " + message).color(ColorTable.DEFAULT.getColor());

        return Component.empty().append(chatMarkComponent).append(nameComponent).append(messageComponent);
    }

    private String getTimeText() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @CommandAlias("forceglobal|fg")
    public static class ForceGlobalCommand extends BaseCommand {
        private List<String> players = new LinkedList<>();

        @Default
        public void OnDefault(Player player) {
            String name = player.getName();

            if(players.contains(name))
                players.remove(name);
            else
                players.add(name);
        }

        @HelpCommand
        public void OnHelp(CommandSender sender) {

        }
    }
}
