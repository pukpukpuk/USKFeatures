package pukpukpuk.uskfeatures;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.intellij.lang.annotations.RegExp;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ComponentUtils {
    private static final Pattern TAG_PATTERN = Pattern.compile("<\\/*(\\w+)>");

    public static Component format(Component component, Component... args) {
        for (int i = 0; i < args.length; i++) {
            Component replacement = args[i];
            @RegExp String pattern = "@" + i;

            component = component.replaceText(builder ->
                    builder.match(pattern).replacement(replacement).once());
        }

        return component;
    }

    public static Component format(String string, Component... args) {
        return format(ColorTable.text(string), args);
    }

    public static Component formatColors(String string) {
        List<MatchResult> results = TAG_PATTERN.matcher(string).results().collect(Collectors.toList());

        for (MatchResult result : results) {
            String group = result.group(1);

            ColorTable color = ColorTable.tryGetByCode(group.toUpperCase());
            if (color == null)
                continue;

            String replacement = result.group().replaceAll(group, color.getHexString());
            string = string.replaceFirst(result.group(), replacement);
        }

        return MiniMessage.miniMessage().deserialize(String.format("<%s>%s", ColorTable.DEFAULT.getHexString(), string));
    }

    public static Component formatColors(String string, ColorTable... colors) {
        for (int i = 0; i < colors.length; i++) {
            @RegExp String pattern = "@c" + i;
            string = string.replaceAll(pattern, colors[i].getHexString());
        }

        return formatColors(string);
    }
}
