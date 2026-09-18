package zone.moddev.patchy.updatecheckers.fabric.api;

import com.unascribed.flexver.FlexVerComparator;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.fabric.FabricVersionHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class FabricApiUpdateChecker extends AbstractUpdateChecker<FabricApiVersion> {

    public FabricApiUpdateChecker() {
        super(FabricApiVersion.class, NotifierConfiguration.<FabricApiVersion>builder(UpdateCheckerType.FABRIC_API)
                .versionComparator((oldVersion, newVersion) -> FlexVerComparator.compare(oldVersion.apiPart(), newVersion.apiPart()))
                .versionKeyExtractor(FabricApiVersion::apiPart)
                .webhookInfo(new WebhookInfo("Fabric API Updates", "https://github.com/fabricmc.png"))
                .build());
    }

    @Override
    protected List<String> getUpdateKeys() throws IOException {
        return FabricVersionHelper.getFabricApiVersions().keySet().stream().toList();
    }

    @Override
    protected Map<String, FabricApiVersion> fetchLatest() throws IOException {
        return FabricVersionHelper.getFabricApiVersions();
    }

    @Override
    protected @NotNull List<EmbedBuilder> getEmbeds(String mcVersion, @Nullable final FabricApiVersion oldVersion, final @NotNull FabricApiVersion newVersion) {
        // First run, just announce the latest version available.
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New Fabric API Update Released!");
        embed.setColor(0xDBD2B5);
        embed.addField("Minecraft Version", mcVersion, true);
        if (oldVersion == null) {
            embed.addField("Fabric API Version", newVersion.fullVersion(), true);
        } else {
            embed.addField("Latest Fabric API Version", "**%s** -> **%s**".formatted(oldVersion.fullVersion(), newVersion.fullVersion()), true);
        }
        return List.of(embed);
    }
}
