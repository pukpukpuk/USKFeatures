package pukpukpuk.uskfeatures.playerlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Set;

@Data
@AllArgsConstructor
public class Message {
    private final String name;
    private final Component component;
    private final Set<String> mayQuote;

    private boolean isDM;

    public Message(String name, Component component) {
        this(name, component, null, false);
    }

    public boolean mayQuoteMessage(Player player) {
        return mayQuote == null || mayQuote.contains(player.getName());
    }
}
