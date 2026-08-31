package pl.fejzu.copyshopkeepers.client;

import net.fabricmc.api.ClientModInitializer;
import pl.fejzu.copyshopkeepers.client.command.ExportCommand;
import pl.fejzu.copyshopkeepers.client.config.ExportConfig;
import pl.fejzu.copyshopkeepers.client.keybinding.CopyKeybinding;

public class FabricModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ExportConfig config = ExportConfig.load();
        CopyKeybinding.register(config);
        ExportCommand.register(config);
    }
}
