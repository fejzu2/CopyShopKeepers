package pl.fejzu.copyshopkeepers.client.export;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import pl.fejzu.copyshopkeepers.client.config.ShopType;
import pl.fejzu.copyshopkeepers.client.util.Messages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public final class ExportFeedback {

    private ExportFeedback() {
    }

    public static MutableText build(String text, int count, String format, ShopType shopType, boolean save) {
        MutableText feedback = Messages.text("copied", Map.of(
                "count", String.valueOf(count),
                "format", format
        ));

        if ("shopkeepers".equals(format)) {
            feedback.append("\n").append(Messages.text("copied-shoptype-line", Map.of("shoptype", shopType.commandLiteral)));
            feedback.append("\n").append(Messages.text("copied-paste-hint", Map.of("datakey", shopType.dataKey)));
        }

        if (save) {
            try {
                Path file = VillagerExportService.save(text, VillagerExportService.fileExtension(format));
                feedback.append("\n").append(Messages.text(
                        "saved-file",
                        Map.of("file", file.toString()),
                        style -> style.withClickEvent(new ClickEvent.OpenFile(file))
                ));
            } catch (IOException e) {
                feedback.append("\n").append(Messages.text("save-failed", Map.of("error", String.valueOf(e.getMessage()))));
            }
        }

        feedback.append("\n").append(Messages.text("spacer"));
        return feedback;
    }
}
