package pukpukpuk.uskfeatures.controllers;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Private;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.USKFeatures;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController implements IController {

    private static final Pattern PING_REGEX = Pattern.compile("^@(.+)");

    private final ToggleGlobalCommand toggleGlobalCommand;

    public ChatController() {
        USKFeatures.getCommandManager().registerCommand(toggleGlobalCommand = new ToggleGlobalCommand());
        USKFeatures.getCommandManager().registerCommand(new ChatCommand());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        toggleGlobalCommand.players.remove(event.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean exclSymbol = message.toCharArray()[0] == '!';
        boolean inverted = toggleGlobalCommand.players.contains(player.getName());
        boolean toGlobal = inverted != exclSymbol;

        if (exclSymbol) {
            message = message.substring(1);
        }

        List<Audience> audiences = getPlayerAudiences(player, toGlobal);
        boolean noOneHasHeard = audiences.size() <= 2 && !toGlobal;

        List<Component> componentList = createMessageComponent(player, message, toGlobal, audiences);

        if (!noOneHasHeard && !toGlobal) {
            Component component = componentList.remove(componentList.size() - 1);

            StringJoiner stringJoiner = new StringJoiner(", ");
            for (int i = 2; i < audiences.size(); i++) {
                Audience a = audiences.get(i);
                if (a instanceof Player)
                    stringJoiner.add(((Player) a).getName());

            }

            Component hoverText = ColorTable.text("Это сообщение увидели: ");
            hoverText = hoverText.append(ColorTable.HIGHLIGHTED.coloredText(stringJoiner.toString()));

            component = component.hoverEvent(HoverEvent.showText(hoverText));
            componentList.add(component);
        }

        Component component = Component.empty();
        for (Component c : componentList)
            component = component.append(c);

        for (Audience audience : audiences)
            audience.sendMessage(component);

        if (noOneHasHeard) {
            player.sendMessage(ColorTable.ERROR.coloredText("Твоё сообщение никто не увидел"));
        }
    }

    private List<Audience> getPlayerAudiences(Player player, boolean toGlobal) {
        List<Audience> audiences = new ArrayList<>();
        audiences.add(Bukkit.getConsoleSender());
        audiences.add(player);

        Bukkit.getOnlinePlayers().forEach(recipient -> {
            if (recipient == player)
                return;

            boolean sameWorld = player.getWorld() == recipient.getWorld();
            boolean inRadius = sameWorld && player.getLocation().distanceSquared(recipient.getLocation()) <= 128 * 128;
            boolean canHear = sameWorld && inRadius;

            if (toGlobal || canHear) {
                audiences.add(recipient);
            }
        });

        return audiences;
    }

    private List<Component> createMessageComponent(Player player, String message, boolean toGlobal, List<Audience> audiences) {
        return new ArrayList<>(List.of(getMarkComponent(toGlobal),
                getNameComponent(player, message, toGlobal),
                ColorTable.text(": "),
                mentionPlayers(message, getMessageColor(message), audiences)));
    }

    private Component getMarkComponent(boolean toGlobal) {
        ColorTable markColor = toGlobal ? ColorTable.GLOBAL_CHAT_MARK : ColorTable.LOCAL_CHAT_MARK;

        String title = toGlobal ? "Глобальный чат" : "Локальный чат";
        String text = toGlobal ? "Это сообщение видят все игроки"
                : "Это сообщение видят только те, кто находится в восьми чанках от вас";

        Component hint = ColorTable.TIME.coloredText(getTimeText())
                .appendNewline()
                .append(markColor.coloredText(title))
                .appendNewline()
                .append(ColorTable.text(text));

        return markColor.coloredText(toGlobal ? "ɢ " : "ʟ ").hoverEvent(HoverEvent.showText(hint));
    }

    private Component getNameComponent(Player player, String message, boolean toGlobal) {
        ColorTable nameColor = toGlobal ? ColorTable.GLOBAL_CHAT_NAME : ColorTable.LOCAL_CHAT_NAME;

        Component hint = ColorTable.text("Нажми, чтобы упомянуть это сообщение")
                .replaceText(ColorTable.GREENTEXT.getReplacementConfig("упомянуть"));

        return nameColor.coloredText(player.getName())
                .hoverEvent(HoverEvent.showText(hint))
                .clickEvent(ClickEvent.runCommand(String.format("/chat @%s > %s", player.getName(), message)));
    }

    private ColorTable getMessageColor(String message) {
        String[] strings = message.split(" ");
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if(checkMention(string) != null) {
                continue;
            }

            return string.startsWith(">") ? ColorTable.GREENTEXT : ColorTable.DEFAULT;
        }

        return ColorTable.DEFAULT;
    }

    private Component mentionPlayers(String message, ColorTable color, List<Audience> audiences) {
        String[] words = message.split(" ");
        Set<Player> mentionedPlayers = new HashSet<>();

        Component component = Component.empty();

        for (String word: words) {
            ColorTable wordColor = color;

            Player mentioned = checkMention(word);
            if(mentioned != null) {
                mentionedPlayers.add(mentioned);

                wordColor = ColorTable.MENTIONED;
                word = (word.charAt(0) != '@' ? "@" : "") + word;
            }

            component = component.append(wordColor.coloredText(word + " "));
        }

        mentionedPlayers.forEach(mentioned -> {
            if(audiences.contains(mentioned)) {
                mentioned.playSound(mentioned, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.PLAYERS, .8f, 1.2f);
            }
        });

        return component;
    }

    private Player checkMention(String string) {
        Matcher matcher = PING_REGEX.matcher(string);

        Player withSymbol = matcher.matches() ? Bukkit.getPlayerExact(matcher.group(1)) : null;
        Player withoutSymbol = Bukkit.getPlayerExact(string);

        return withSymbol != null ? withSymbol : withoutSymbol;
    }

    private String getTimeText() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @CommandAlias("toggleglobal|tg")
    public static class ToggleGlobalCommand extends BaseCommand {
        private List<String> players = new LinkedList<>();

        @Default
        public void OnDefault(Player player) {
            String name = player.getName();

            if (players.contains(name))
                players.remove(name);
            else
                players.add(name);
        }
    }

    @CommandAlias("chat")
    @Private
    public static class ChatCommand extends BaseCommand {
        @Default
        public void OnDefault(Player player, String message) {
            player.chat(message);
        }
    }
}
