package pl.fejzu.copyshopkeepers.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExportConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("CopyShopKeepers");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("copyshopkeepers.json");

    public String defaultFormat = "shopkeepers";
    public String defaultShopType = ShopType.ADMIN.commandLiteral;
    public boolean alsoSaveToFile = true;

    public ShopType shopType() {
        return ShopType.fromCommandLiteral(defaultShopType);
    }

    public static ExportConfig load() {
        if (Files.exists(PATH)) {
            try {
                String json = Files.readString(PATH);
                ExportConfig cfg = GSON.fromJson(json, ExportConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Failed to load copyshopkeepers.json, using default settings", e);
            }
        }
        ExportConfig fresh = new ExportConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("Failed to save copyshopkeepers.json", e);
        }
    }
}
