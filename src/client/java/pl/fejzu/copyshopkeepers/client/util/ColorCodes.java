package pl.fejzu.copyshopkeepers.client.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.UnaryOperator;

public final class ColorCodes {

    private ColorCodes() {
    }

    public static MutableText parse(String raw) {
        return parse(raw, UnaryOperator.identity());
    }

    public static MutableText parse(String raw, UnaryOperator<Style> extraStyle) {
        MutableText result = Text.literal("");
        Style baseStyle = extraStyle.apply(Style.EMPTY);
        Style style = baseStyle;
        StringBuilder current = new StringBuilder();

        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);

            if (c == '&' && i + 7 < raw.length() && raw.charAt(i + 1) == '#' && isHex6(raw, i + 2)) {
                if (current.length() > 0) {
                    result.append(Text.literal(current.toString()).setStyle(style));
                    current.setLength(0);
                }
                int rgb = Integer.parseInt(raw.substring(i + 2, i + 8), 16);
                style = style.withColor(rgb);
                i += 8;
                continue;
            }

            if (c == '&' && i + 1 < raw.length() && Formatting.byCode(raw.charAt(i + 1)) != null) {
                if (current.length() > 0) {
                    result.append(Text.literal(current.toString()).setStyle(style));
                    current.setLength(0);
                }
                Formatting formatting = Formatting.byCode(raw.charAt(i + 1));
                style = (formatting == Formatting.RESET) ? baseStyle : style.withFormatting(formatting);
                i += 2;
                continue;
            }

            current.append(c);
            i++;
        }

        if (current.length() > 0) {
            result.append(Text.literal(current.toString()).setStyle(style));
        }
        return result;
    }

    private static boolean isHex6(String raw, int start) {
        for (int i = start; i < start + 6; i++) {
            if (Character.digit(raw.charAt(i), 16) == -1) {
                return false;
            }
        }
        return true;
    }
}
