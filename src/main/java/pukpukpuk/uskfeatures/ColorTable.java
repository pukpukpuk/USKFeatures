package pukpukpuk.uskfeatures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;

@AllArgsConstructor
public enum ColorTable {
    DEFAULT(0xe6eeed),
    NORMAL(0x6ec077),
    NETHER(0x9d4343),
    THE_END(0xa17695);

    @Getter
    private int color;

    public static ColorTable tryGet(String string) {
        try {
            return ColorTable.valueOf(string);
        } catch (Exception e) {
            return DEFAULT;
        }
    }
}
