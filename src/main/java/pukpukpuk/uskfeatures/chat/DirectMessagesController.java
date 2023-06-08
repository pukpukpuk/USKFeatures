package pukpukpuk.uskfeatures.chat;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import pukpukpuk.uskfeatures.ColorTable;
import pukpukpuk.uskfeatures.ComponentUtils;
import pukpukpuk.uskfeatures.Controller;
import pukpukpuk.uskfeatures.USKFeatures;
import pukpukpuk.uskfeatures.playerlist.Message;

import java.util.List;
import java.util.Set;


@Controller
public class DirectMessagesController implements Listener {

    public DirectMessagesController() {
        USKFeatures.getCommandManager().registerCommand(new DirectMessageCommand());
    }

    @CommandAlias("dm")
    public static class DirectMessageCommand extends BaseCommand {

        @Default
        @CommandCompletion("@players")
        public void OnDefault(Player sender, @Name("recipient") @Single String recipientName, @Name("message") String message) {
            Player recipient = Bukkit.getPlayerExact(recipientName);
            if(recipient == null) {
                sender.sendMessage(ColorTable.ERROR.coloredText("Этот игрок не в сети"));
                return;
            }

            Component messageComponent = ChatController.getInstance().mentionPlayers(sender, message, List.of(sender, recipient));

            boolean toHimself = sender == recipient;
            DMComponent dmComponent = new DMComponent(recipientName, sender.getName(), messageComponent, ChatController.getInstance().messages.size());

            sender.sendMessage(dmComponent.get(MessageType.FOR_SENDER));
            if(!toHimself)
                recipient.sendMessage(dmComponent.get(MessageType.FOR_RECIPIENT));

            ChatController.getInstance().messages.add(
                    new Message(
                            sender.getName(),
                            dmComponent.get(MessageType.FOR_ALL),
                            toHimself ? Set.of(sender.getName()) :
                                    Set.of(sender.getName(), recipientName),
                            true
                    )
            );
        }

        @AllArgsConstructor
        private static class DMComponent {
            private String recipientName;
            private String senderName;
            private Component message;
            private int id;

            public Component get(MessageType type) {
                boolean toHimself = senderName.equals(recipientName);

                Component markComponent = ColorTable.DM_CHAT.coloredText("✉")
                        .hoverEvent(
                                HoverEvent.showText(
                                        ComponentUtils.formatColors(
                                                String.format(
                                                        "<@c0>Айди</@c0> этого сообщения: <@c0>%s</@c0>." +
                                                                "\nНажми, чтобы <@c0>скопировать</@c0> его" +
                                                                "\n" +
                                                                "\n<@c1>⌚</@c1> Время отправки этого сообщения: <@c1>%s</@c1>",
                                                        id,
                                                        ChatController.getTimeText()
                                                ),
                                                ColorTable.DM_CHAT,
                                                ColorTable.TIME
                                        )
                                )
                        ).clickEvent(
                                ClickEvent.copyToClipboard(String.format(">%s ", id))
                        );

                String first = type == MessageType.FOR_SENDER ? "Я" : senderName;
                String second = type == MessageType.FOR_RECIPIENT ? "Мне" : (type == MessageType.FOR_SENDER && toHimself) ? "Себе" : recipientName;

                Component nicknamesComponent = ColorTable.text(String.format("%s » %s", first, second))
                        .hoverEvent(
                                HoverEvent.showText(
                                        ComponentUtils.formatColors(
                                                "Нажми, чтобы <@c0>ответить</@c0> на это сообщение",
                                                ColorTable.DM_CHAT
                                        )
                                )
                        )
                        .clickEvent(
                                ClickEvent.suggestCommand(String.format("/dm %s >%s ", type == MessageType.FOR_SENDER ? recipientName : senderName, id))
                        );

                return ComponentUtils.format(
                        "@0 @1: @2",
                        markComponent,
                        nicknamesComponent,
                        message
                );
            }
        }

        private enum MessageType {
            FOR_SENDER,
            FOR_RECIPIENT,
            FOR_ALL;
        }
    }
}
