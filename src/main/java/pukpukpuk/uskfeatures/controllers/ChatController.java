package pukpukpuk.uskfeatures.controllers;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
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
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.USKFeatures;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController implements IController {

    private static final Pattern MENTION_REGEX = Pattern.compile("^@(.+)");

    private final ToggleGlobalCommand toggleGlobalCommand;

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
        String message = removeQuoteFlood(event.getMessage());

        boolean exclSymbol = message.startsWith("!");
        boolean inverted = toggleGlobalCommand.players.contains(player.getName());
        boolean toGlobal = inverted != exclSymbol;

        if (exclSymbol)
            message = message.substring(1);

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

    private List<Component> createMessageComponent(Player player, String message, boolean toGlobal, List<Audience> audiences) {
        return new ArrayList<>(List.of(getMarkComponent(toGlobal),
                getNameComponent(player, message, toGlobal),
                ColorTable.text(": "),
                mentionPlayers(message, audiences)));
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
                .clickEvent(ClickEvent.suggestCommand(String.format("%s@%s >%s", toGlobal ? "!" : "", player.getName(), message)));
    }

    private String removeQuoteFlood(String originalMessage) {
        StringBuilder stringBuilder = new StringBuilder();

        int quotesReached = 0;
        boolean lastWord = false;

        char[] characters = originalMessage.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            char c = characters[i];

            stringBuilder.append(c);
            if(lastWord) {
                if(characters[i] == ' ') {
                    stringBuilder.deleteCharAt(i);
                    stringBuilder.append("...");
                    break;
                }
                continue;
            }

            if(c == '>') {
                quotesReached++;
                if(quotesReached == 2) {
                    if(i == characters.length-1)
                        break;

                    if(characters[i+1] == ' ') {
                        stringBuilder.append(" ");
                        i++;
                    }

                    lastWord = true;
                }
            }

        }

        return stringBuilder.toString();

        //String[] words = originalMessage.split(" ");

        //int quotesReached = 0;
        //StringBuilder stringBuilder = new StringBuilder();

        //for (int i = 0; i < words.length; i++) {
        //    String word = words[i];

        //    if(quotesReached >= 2) {
        //        stringBuilder.append(word).append("...");
        //        break;
        //    } else {
        //        stringBuilder.append(word).append(" ");
        //    }

        //    if (word.startsWith(">")) {
        //        quotesReached++;

        //        if(word.length() > 1) {

        //        }

        //    }

        //}

        //return stringBuilder.toString();
    }

    private Component mentionPlayers(String message, List<Audience> audiences) {
        String[] words = message.split(" ");
        Set<Player> mentionedPlayers = new HashSet<>();

        Component component = Component.empty();

        boolean quoteStarted = false;
        for (String word : words) {
            if (word.startsWith(">"))
                quoteStarted = true;

            ColorTable wordColor = quoteStarted ? ColorTable.GREENTEXT : ColorTable.DEFAULT;

            Player mentioned = checkMention(word);
            if (mentioned != null && !quoteStarted) {
                mentionedPlayers.add(mentioned);

                wordColor = ColorTable.MENTIONED;
                if (!word.startsWith("@"))
                    word = "@" + word;
            }

            component = component.append(wordColor.coloredText(word + " "));
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
