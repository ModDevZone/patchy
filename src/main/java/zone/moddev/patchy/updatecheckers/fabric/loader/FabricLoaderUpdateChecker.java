package zone.moddev.patchy.updatecheckers.fabric.loader;

import com.unascribed.flexver.FlexVerComparator;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.fabric.FabricVersionHelper;

import java.util.List;

public final class FabricLoaderUpdateChecker extends AbstractUpdateChecker.Single<FabricLoaderVersion> {

    // TODO config?
    private static final boolean INCLUDE_BETA_VERSIONS = true;

    public FabricLoaderUpdateChecker() {
        super(FabricLoaderVersion.class, NotifierConfiguration.<FabricLoaderVersion>builder(UpdateCheckerType.FABRIC_LOADER)
                .versionComparator((oldVersion, newVersion) -> FlexVerComparator.compare(oldVersion.version(), newVersion.version()))
                .versionKeyExtractor(FabricLoaderVersion::version)
                .webhookInfo(new WebhookInfo("Fabric Loader Updates", "https://github.com/fabricmc.png"))
                .build());
    }

    @Nullable
    @Override
    protected FabricLoaderVersion fetchLatestSingle() {
        return FabricVersionHelper.getLatestLoaderVersion(INCLUDE_BETA_VERSIONS).orElseGet(() -> {
            Patchy.LOGGER.error(loggingMarker, "Unable to retrieve latest fabric loader version!");
            return null;
        });
    }

    @Override
    protected List<EmbedBuilder> getEmbedsSingle(@Nullable final FabricLoaderVersion oldVersion, final @NotNull FabricLoaderVersion newVersion) {
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(newVersion.stable() ? "New Fabric Loader Update Released!" : "New Fabric Loader Beta Released!");
        embed.setColor(newVersion.stable() ? 0xDBD2B5 : 0xDBC3B5);

        if (oldVersion == null) {
            embed.addField("Fabric Loader Version", newVersion.version(), true);
        } else {
            embed.addField("Latest Fabric Loader Version", "**%s** -> **%s**".formatted(oldVersion.version(), newVersion.version()), true);
        }
        return List.of(embed);
    }
}
