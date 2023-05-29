package pukpukpuk.uskfeatures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

@AllArgsConstructor
public enum ColorTable {
    DEFAULT(0xe6eeed),
    NORMAL_DIMENSION(0x6ec077),
    NETHER_DIMENSION(0x9d4343),
    THE_END_DIMENSION(0xa17695),
    TIME(0xaaeeea),
    HIGHLIGHTED(0xd5f893),
    GLOBAL_CHAT_MARK(0xe39764),
    LOCAL_CHAT_MARK(0xfceba8),
    GLOBAL_CHAT_NAME(0xa9bcbf),
    LOCAL_CHAT_NAME(0xe6eeed),
    ERROR(0xbc5960),
    SUCCESS(0x4e9363),
    WARNING(0xf5c47c),
    MENTIONED(0xf5c47c),
    GREENTEXT(0x7fbbdc),
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

    public Component coloredText(String content) {
        return Component.text(content, getColor());
    }

    public Component coloredText(int value) {
        return coloredText(String.valueOf(value));
    }

    public Component coloredText(double value) {
        return coloredText(String.valueOf(value));
    }

    public Component coloredText(float value) {
        return coloredText(String.valueOf(value));
    }

    public static Component text(String content) {
        return ColorTable.DEFAULT.coloredText(content);
    }

    public static Component text(int value) {
        return text(String.valueOf(value));
    }

    public static Component text(double value) {
        return text(String.valueOf(value));
    }

    public static Component text(float value) {
        return text(String.valueOf(value));
    }
}
