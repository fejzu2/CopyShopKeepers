package pl.fejzu.copyshopkeepers.client.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.village.TradeOffer;
import pl.fejzu.copyshopkeepers.client.config.ExportConfig;
import pl.fejzu.copyshopkeepers.client.config.ShopType;
import pl.fejzu.copyshopkeepers.client.export.ExportFeedback;
import pl.fejzu.copyshopkeepers.client.export.VillagerExportService;
import pl.fejzu.copyshopkeepers.client.util.Messages;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ExportCommand {

    private ExportCommand() {
    }

    public static void register(ExportConfig config) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = literal("villagerexport")
                    .executes(ctx -> run(ctx, config.defaultFormat, config.shopType(), false))
                    .then(literal("save").executes(ctx -> run(ctx, config.defaultFormat, config.shopType(), true)))
                    .then(literal("help").executes(ExportCommand::help))
                    .then(literal("format")
                            .then(literal("readable").executes(ctx -> setFormat(ctx, config, "readable")))
                            .then(literal("shopkeepers").executes(ctx -> setFormat(ctx, config, "shopkeepers")))
                            .then(literal("json").executes(ctx -> setFormat(ctx, config, "json"))))
                    .then(literal("shoptype")
                            .then(literal(ShopType.ADMIN.commandLiteral)
                                    .executes(ctx -> setShopType(ctx, config, ShopType.ADMIN)))
                            .then(literal(ShopType.TRADING_PLAYER.commandLiteral)
                                    .executes(ctx -> setShopType(ctx, config, ShopType.TRADING_PLAYER))));

            for (String fmt : new String[] {"readable", "json"}) {
                root.then(literal(fmt)
                        .executes(ctx -> run(ctx, fmt, config.shopType(), false))
                        .then(literal("save").executes(ctx -> run(ctx, fmt, config.shopType(), true))));
            }

            var shopkeepers = literal("shopkeepers")
                    .executes(ctx -> run(ctx, "shopkeepers", config.shopType(), false))
                    .then(literal("save").executes(ctx -> run(ctx, "shopkeepers", config.shopType(), true)));
            for (ShopType shopType : ShopType.values()) {
                shopkeepers.then(literal(shopType.commandLiteral)
                        .executes(ctx -> run(ctx, "shopkeepers", shopType, false))
                        .then(literal("save").executes(ctx -> run(ctx, "shopkeepers", shopType, true))));
            }
            root.then(shopkeepers);

            dispatcher.register(root);
        });
    }

    private static int help(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Messages.text("command.help"));
        return 1;
    }

    private static int run(
            CommandContext<FabricClientCommandSource> ctx,
            String format,
            ShopType shopType,
            boolean save
    ) {
        FabricClientCommandSource source = ctx.getSource();
        MinecraftClient client = source.getClient();

        Optional<List<TradeOffer>> offersOpt = VillagerExportService.currentOffers(client);
        if (offersOpt.isEmpty()) {
            source.sendError(Messages.text("no-trade-window"));
            return 0;
        }
        List<TradeOffer> offers = offersOpt.get();
        if (offers.isEmpty()) {
            source.sendError(Messages.text("no-trades"));
            return 0;
        }

        String text = VillagerExportService.format(format, offers, client.player.getRegistryManager(), shopType);
        client.keyboard.setClipboard(text);

        source.sendFeedback(ExportFeedback.build(text, offers.size(), format, shopType, save));
        return 1;
    }

    private static int setFormat(CommandContext<FabricClientCommandSource> ctx, ExportConfig config, String format) {
        config.defaultFormat = format;
        config.save();
        ctx.getSource().sendFeedback(Messages.text("format-changed", Map.of("format", format)));
        return 1;
    }

    private static int setShopType(CommandContext<FabricClientCommandSource> ctx, ExportConfig config, ShopType shopType) {
        config.defaultShopType = shopType.commandLiteral;
        config.save();
        ctx.getSource().sendFeedback(Messages.text("shoptype-changed", Map.of(
                "shoptype", shopType.commandLiteral,
                "description", shopType.description
        )));
        return 1;
    }
}
