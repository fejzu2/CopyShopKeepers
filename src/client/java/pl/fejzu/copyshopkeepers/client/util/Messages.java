package pl.fejzu.copyshopkeepers.client.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class Messages {

    private static final String RESOURCE_PATH = "/assets/copyshopkeepers/messages/messages.json";
    private static final Logger LOGGER = LoggerFactory.getLogger("CopyShopKeepers");
    private static final Map<String, List<String>> MESSAGES = load();

    private Messages() {
    }

    private static Map<String, List<String>> load() {
        try (InputStream in = Messages.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                LOGGER.warn("Could not find {} - messages will fall back to their keys.", RESOURCE_PATH);
                return Map.of();
            }
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> parsed = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
            return parsed != null ? parsed : Map.of();
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Failed to load {}", RESOURCE_PATH, e);
            return Map.of();
        }
    }

    private static String raw(String key) {
        List<String> lines = MESSAGES.get(key);
        if (lines == null || lines.isEmpty()) {
            return "&c[CopyShopKeepers] Brak komunikatu dla klucza: " + key;
        }
        return String.join("\n", lines);
    }

    private static String substitute(String template, Map<String, String> placeholders) {
        String result = template.replace("%prefix%", raw("prefix"));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    public static MutableText text(String key) {
        return text(key, Map.of());
    }

    public static MutableText text(String key, Map<String, String> placeholders) {
        return ColorCodes.parse(substitute(raw(key), placeholders));
    }

    public static MutableText text(String key, Map<String, String> placeholders, UnaryOperator<Style> extraStyle) {
        return ColorCodes.parse(substitute(raw(key), placeholders), extraStyle);
    }
}
