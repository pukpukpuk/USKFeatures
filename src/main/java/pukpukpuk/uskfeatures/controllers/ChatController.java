package pukpukpuk.uskfeatures.controllers;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.javatuples.Pair;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.USKFeatures;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController implements IController {

    private static final Pattern MENTION_REGEX = Pattern.compile("^@(.+)");
    private static final Pattern QUOTE_REGEX = Pattern.compile("^>(\\d+)");

    private final ToggleGlobalCommand toggleGlobalCommand;
    private final List<Pair<String, Component>> messages = new LinkedList<>();

    public ChatController() {
        USKFeatures.getCommandManager().registerCommand(toggleGlobalCommand = new ToggleGlobalCommand());
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

        boolean exclSymbol = message.startsWith("!");
        boolean inverted = toggleGlobalCommand.players.contains(player.getName());
        boolean toGlobal = inverted != exclSymbol;

        if (exclSymbol)
            message = message.substring(1).trim();

        List<Audience> audiences = getPlayerAudiences(player, toGlobal);
        boolean noOneHasHeard = audiences.size() <= 2 && !toGlobal;

        int messageId = messages.size();

        List<Component> componentList = createMessageComponent(player, message, toGlobal, audiences, messageId);

        if (!noOneHasHeard && !toGlobal) {
            Component component = componentList.remove(componentList.size() - 1);

            StringJoiner stringJoiner = new StringJoiner(", ");
            for (int i = 2; i < audiences.size(); i++) {
                Audience a = audiences.get(i);
                if (a instanceof Player)
                    stringJoiner.add(((Player) a).getName());
            }

            Component hoverText = ColorTable.text("Это сообщение увидели: ")
                    .append(ColorTable.HIGHLIGHTED.coloredText(stringJoiner.toString()));

            componentList.add(component.hoverEvent(HoverEvent.showText(hoverText)));
        }

        Component component = Component.empty();
        for (Component c : componentList)
            component = component.append(c);

        messages.add(new Pair<>(player.getName(), component));

        for (Audience audience : audiences)
            audience.sendMessage(component);

        if (noOneHasHeard)
            player.sendMessage(ColorTable.ERROR.coloredText("Твоё сообщение никто не увидел"));
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

    private List<Component> createMessageComponent(Player player, String message, boolean toGlobal, List<Audience> audiences, int messageId) {
        return new ArrayList<>(List.of(getMarkComponent(toGlobal, messageId),
                getNameComponent(player, message, toGlobal, messageId),
                ColorTable.text(": "),
                mentionPlayers(message, audiences)));
    }

    private Component getMarkComponent(boolean toGlobal, int messageId) {
        ColorTable color = ColorTable.QUOTE;

        Component hint = ColorTable.text("Айди этого сообщения: ")
                                .replaceText(color.getReplacementConfig("Айди"))
                .append(color.coloredText(messageId)).append(ColorTable.text("."))
                .appendNewline()
                .append(
                        ColorTable.text("Нажми, чтобы скопировать его")
                                .replaceText(color.getReplacementConfig("скопировать")))
                .appendNewline()
                .appendNewline()
                .append(
                        ColorTable.text("⌚ Время отправки этого сообщения: ")
                                .replaceText(ColorTable.TIME.getReplacementConfig("⌚")))
                .append(ColorTable.TIME.coloredText(getTimeText()));

        ColorTable markColor = toGlobal ? ColorTable.GLOBAL_CHAT_MARK : ColorTable.LOCAL_CHAT_MARK;
        return markColor.coloredText(toGlobal ? "ɢ " : "ʟ ")
                .hoverEvent(HoverEvent.showText(hint))
                .clickEvent(ClickEvent.copyToClipboard(String.format(">%s ", messageId)));
    }

    private Component getNameComponent(Player player, String message, boolean toGlobal, int messageId) {
        ColorTable nameColor = toGlobal ? ColorTable.GLOBAL_CHAT_NAME : ColorTable.LOCAL_CHAT_NAME;

        Component hint = ColorTable.text("Нажми, чтобы упомянуть это сообщение")
                .replaceText(ColorTable.QUOTE.getReplacementConfig("упомянуть"));

        return nameColor.coloredText(player.getName())
                .hoverEvent(HoverEvent.showText(hint))
                .clickEvent(ClickEvent.suggestCommand(String.format("%s>%s ", toGlobal ? "!" : "", messageId)));
    }

    private Component mentionPlayers(String message, List<Audience> audiences) {
        String[] words = message.split(" ");
        Set<Player> mentionedPlayers = new HashSet<>();

        Component component = Component.empty();

        for (String word : words) {
            ColorTable wordColor = ColorTable.DEFAULT;
            Component hint = null;

            Player mentioned = checkMention(word);
            if (mentioned != null) {
                mentionedPlayers.add(mentioned);

                wordColor = ColorTable.MENTIONED;
                if (!word.startsWith("@"))
                    word = "@" + word;
            }

            Matcher quoteMatcher = QUOTE_REGEX.matcher(word);
            if (quoteMatcher.matches()) {
                int id = Integer.parseInt(quoteMatcher.group(1));

                if (id >= 0 && id < messages.size()) {
                    wordColor = ColorTable.QUOTE;
                    Pair<String, Component> pair = messages.get(id);

                    Player player = Bukkit.getPlayerExact(pair.getValue0());
                    if(player != null)
                        mentionedPlayers.add(player);

                    hint = ColorTable.text("В ответ на:")
                            .appendNewline()
                            .appendSpace()
                            .append(pair.getValue1());
                }
            }

            Component subComponent = wordColor.coloredText(word + " ");
            if(hint != null)
                subComponent = subComponent.hoverEvent(HoverEvent.showText(hint));

            component = component.append(subComponent);
        }

        mentionedPlayers.forEach(mentioned -> {
            if (audiences.contains(mentioned)) {
                mentioned.playSound(mentioned, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.PLAYERS, .8f, 1.2f);
            }
        });

        return component;
    }

    private Player checkMention(String string) {
        Matcher matcher = MENTION_REGEX.matcher(string);

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
        private final List<String> players = new LinkedList<>();

        @Default
        public void OnDefault(Player player) {
            String name = player.getName();

            boolean removed = players.contains(name);
            if (removed)
                players.remove(name);
            else
                players.add(name);

            player.sendMessage(ColorTable.HIGHLIGHTED.coloredText(" ☂ ")
                    .append(ColorTable.text("Инверсия чата "))
                    .append(ColorTable.HIGHLIGHTED.coloredText(removed ? "выключена" : "включена")));
        }
    }
}
