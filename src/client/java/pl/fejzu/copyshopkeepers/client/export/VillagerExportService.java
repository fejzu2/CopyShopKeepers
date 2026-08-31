package pl.fejzu.copyshopkeepers.client.export;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import pl.fejzu.copyshopkeepers.client.config.ShopType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VillagerExportService {

    private VillagerExportService() {
    }

    public static Optional<List<TradeOffer>> currentOffers(MinecraftClient client) {
        if (client.player == null) {
            return Optional.empty();
        }
        if (!(client.player.currentScreenHandler instanceof MerchantScreenHandler merchantHandler)) {
            return Optional.empty();
        }
        TradeOfferList recipes = merchantHandler.getRecipes();
        return Optional.of(new ArrayList<>(recipes));
    }

    public static String format(
            String format,
            List<TradeOffer> offers,
            DynamicRegistryManager registries,
            ShopType shopType
    ) {
        return switch (format) {
            case "shopkeepers" -> toShopkeepersFormat(offers, registries, shopType);
            case "json" -> toJsonFormat(offers, registries);
            default -> toReadableFormat(offers);
        };
    }

    public static String fileExtension(String format) {
        return "json".equals(format) ? "json" : "yml";
    }

    public static Path save(String text, String extension) throws IOException {
        Path dir = FabricLoader.getInstance().getGameDir().resolve("villager-export");
        Files.createDirectories(dir);
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
        Path file = dir.resolve("villager-" + timestamp + "." + extension);
        Files.writeString(file, text);
        return file;
    }

    private static String toReadableFormat(List<TradeOffer> offers) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Trade export (").append(offers.size()).append(" trade(s))\n");
        int i = 1;
        for (TradeOffer offer : offers) {
            sb.append(i).append(") ").append(describe(offer.getOriginalFirstBuyItem()));
            ItemStack item2 = offer.getDisplayedSecondBuyItem();
            if (!item2.isEmpty()) {
                sb.append(" + ").append(describe(item2));
            }
            sb.append(" -> ").append(describe(offer.getSellItem())).append('\n');
            i++;
        }
        return sb.toString();
    }

    private static String describe(ItemStack stack) {
        return stack.getCount() + "x " + stack.getName().getString();
    }

    private static String toShopkeepersFormat(
            List<TradeOffer> offers,
            DynamicRegistryManager registries,
            ShopType shopType
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(shopType.dataKey).append(":\n");
        int i = 1;
        for (TradeOffer offer : offers) {
            sb.append("    \"").append(i).append("\":\n");
            sb.append("      resultItem:\n");
            sb.append(itemToShopkeepersYaml(offer.getSellItem(), "        ", registries));
            sb.append("      item1:\n");
            sb.append(itemToShopkeepersYaml(offer.getOriginalFirstBuyItem(), "        ", registries));
            ItemStack item2 = offer.getDisplayedSecondBuyItem();
            if (!item2.isEmpty()) {
                sb.append("      item2:\n");
                sb.append(itemToShopkeepersYaml(item2, "        ", registries));
            }
            i++;
        }
        return sb.toString();
    }

    private static String itemToShopkeepersYaml(
            ItemStack stack,
            String indent,
            DynamicRegistryManager registries
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("DataVersion: ").append(SharedConstants.WORLD_VERSION).append('\n');
        sb.append(indent).append("id: ").append(Registries.ITEM.getId(stack.getItem())).append('\n');
        sb.append(indent).append("count: ").append(stack.getCount()).append('\n');

        NbtCompound root = ItemNbtCodec.encode(stack, registries);
        if (root != null && root.get("components") instanceof NbtCompound components
                && !components.getKeys().isEmpty()) {
            sb.append(indent).append("components:\n");
            String inner = indent + "  ";
            for (String key : components.getKeys()) {
                NbtElement value = components.get(key);
                sb.append(inner).append(key).append(": ")
                        .append(yamlSingleQuote(String.valueOf(value))).append('\n');
            }
        }
        return sb.toString();
    }

    private static String yamlSingleQuote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }

    private static String toJsonFormat(List<TradeOffer> offers, DynamicRegistryManager registries) {
        JsonArray array = new JsonArray();
        for (TradeOffer offer : offers) {
            JsonObject obj = new JsonObject();
            obj.add("resultItem", itemToJson(offer.getSellItem(), registries));
            obj.add("item1", itemToJson(offer.getOriginalFirstBuyItem(), registries));
            ItemStack item2 = offer.getDisplayedSecondBuyItem();
            if (!item2.isEmpty()) {
                obj.add("item2", itemToJson(item2, registries));
            }
            array.add(obj);
        }
        JsonObject root = new JsonObject();
        root.add("trades", array);
        return new GsonBuilder().setPrettyPrinting().create().toJson(root);
    }

    private static JsonElement itemToJson(ItemStack stack, DynamicRegistryManager registries) {
        NbtCompound root = ItemNbtCodec.encode(stack, registries);
        if (root == null) {
            JsonObject fallback = new JsonObject();
            fallback.addProperty("id", Registries.ITEM.getId(stack.getItem()).toString());
            fallback.addProperty("count", stack.getCount());
            return fallback;
        }
        return ItemNbtCodec.toJson(root);
    }
}
