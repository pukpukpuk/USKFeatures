package pukpukpuk.uskfeatures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

@AllArgsConstructor
public enum ColorTable {
    DEFAULT(0xe6eeed),
    NORMAL(0x6ec077),
    NETHER(0x9d4343),
    THE_END(0xa17695),
    TIME(0xaaeeea),
    HIGHLIGHTED(0xd5f893),
    GLOBAL_CHAT_MARK(0xe39764),
    LOCAL_CHAT_MARK(0xfceba8),
    GLOBAL_CHAT_NAME(0xa9bcbf),
    LOCAL_CHAT_NAME(0xe6eeed),

    ;

    @Getter
    private int hex;

    public static ColorTable tryGet(String string) {
        try {
            return ColorTable.valueOf(string);
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    public TextColor getColor() {
        return TextColor.color(hex);
    }
}
