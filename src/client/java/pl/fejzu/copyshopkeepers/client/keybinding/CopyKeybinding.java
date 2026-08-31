package pl.fejzu.copyshopkeepers.client.keybinding;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import org.lwjgl.glfw.GLFW;
import pl.fejzu.copyshopkeepers.client.config.ExportConfig;
import pl.fejzu.copyshopkeepers.client.config.ShopType;
import pl.fejzu.copyshopkeepers.client.export.ExportFeedback;
import pl.fejzu.copyshopkeepers.client.export.VillagerExportService;
import pl.fejzu.copyshopkeepers.client.util.Messages;

import java.util.List;
import java.util.Optional;

public final class CopyKeybinding {

    private static KeyBinding keyBinding;
    private static ExportConfig config;

    private CopyKeybinding() {
    }

    public static void register(ExportConfig exportConfig) {
        config = exportConfig;

        KeyBinding.Category category = KeyBinding.Category.create(
                Identifier.of("copyshopkeepers", "main")
        );
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.copyshopkeepers.copy",
                GLFW.GLFW_KEY_C,
                category
        ));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenKeyboardEvents.allowKeyPress(screen).register((s, keyInput) -> {
                    if (!keyBinding.matchesKey(keyInput)) {
                        return true;
                    }
                    if ((keyInput.modifiers() & GLFW.GLFW_MOD_CONTROL) == 0) {
                        return true;
                    }
                    if (client.player == null
                            || !(client.player.currentScreenHandler instanceof MerchantScreenHandler)) {
                        return true;
                    }
                    doExport(client);
                    return false;
                })
        );
    }

    private static void doExport(MinecraftClient client) {
        Optional<List<TradeOffer>> offersOpt = VillagerExportService.currentOffers(client);
        if (offersOpt.isEmpty()) {
            return;
        }
        List<TradeOffer> offers = offersOpt.get();
        if (offers.isEmpty()) {
            client.player.sendMessage(Messages.text("no-trades"), false);
            return;
        }

        ShopType shopType = config.shopType();
        String text = VillagerExportService.format(
                config.defaultFormat, offers, client.player.getRegistryManager(), shopType);
        client.keyboard.setClipboard(text);

        client.player.sendMessage(
                ExportFeedback.build(text, offers.size(), config.defaultFormat, shopType, config.alsoSaveToFile),
                false
        );
    }
}
