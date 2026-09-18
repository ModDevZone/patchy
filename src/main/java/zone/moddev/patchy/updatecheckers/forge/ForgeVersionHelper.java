package zone.moddev.patchy.updatecheckers.forge;

import com.unascribed.flexver.FlexVerComparator;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;

import java.io.IOException;
import java.util.Map;

public final class ForgeVersionHelper extends SharedVersionHelpers {

    private ForgeVersionHelper() {
        // Prevent instantiation
    }

    private static final String METADATA_URL = "https://maven.minecraftforge.net/releases/net/minecraftforge/forge/maven-metadata.xml";

    /**
     * Gets a map of the latest Forge version for each Minecraft version.
     *
     * @return A map of Minecraft versions to the latest corresponding Forge version.
     */
    public static Map<String, ForgeVersion> getForgeVersions() throws IOException {
        return getVersionsByMinecraftVersion(
                METADATA_URL,
                ForgeVersion::new,
                ForgeVersionHelper::getMinecraftVersionFromForge,
                (oldVersion, newVersion) -> FlexVerComparator.compare(oldVersion.id(), newVersion.id())
        );
    }

    /**
     * Get the Minecraft version from the Forge version number.
     *
     * @param forgeVersion The full Forge version string we need to get the Minecraft version information from.
     * @return The Minecraft version that this Forge build was released for.
     */
    public static String getMinecraftVersionFromForge(ForgeVersion forgeVersion) {
        return forgeVersion.id().split("-")[0];
    }
}
