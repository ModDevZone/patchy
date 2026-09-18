package zone.moddev.patchy.updatecheckers.neoforge;

import com.unascribed.flexver.FlexVerComparator;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public final class NeoForgeVersionHelper extends SharedVersionHelpers {
    private NeoForgeVersionHelper() {
        // Prevent instantiation
    }

    private static final String METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";

    /**
     * Gets a map of the latest NeoForge version for each Minecraft version.
     *
     * @return A map of Minecraft versions to the latest corresponding NeoForge version.
     */
    public static Map<String, NeoForgeVersion> getNeoForgeVersions() throws IOException {
        return getVersionsByMinecraftVersion(
                METADATA_URL,
                NeoForgeVersion::new,
                NeoForgeVersionHelper::getMinecraftVersionFromNeoForge,
                (oldVersion, newVersion) -> FlexVerComparator.compare(oldVersion.id(), newVersion.id())
        );
    }

    /**
     * Extracts the Minecraft version from a NeoForge version string.
     * <p>
     * NeoForge uses two versioning schemes:
     * <ul>
     *     <li><b>Legacy (for MC <= 1.21.x):</b> The format is {@code A.B.C}, corresponding to Minecraft {@code 1.A.B}.
     *     For example, NeoForge {@code 20.2.59} is for Minecraft {@code 1.20.2}.</li>
     *     <li><b>New (for MC >= 26.x):</b> The format is {@code year.release.patch.build}. The Minecraft version is
     *     the first three parts. For example, NeoForge {@code 26.1.0.5} is for Minecraft {@code 26.1.0}.</li>
     * </ul>
     *
     * @param neoForgeVersion The full version string of the NeoForge build.
     * @return The corresponding Minecraft version, or {@code null} if it cannot be determined.
     */
    @Nullable
    public static String getMinecraftVersionFromNeoForge(NeoForgeVersion neoForgeVersion) {
        // Strip suffixes like -beta
        final String versionCore = neoForgeVersion.id().split("-")[0];
        final String[] parts = versionCore.split("\\.");

        if (parts.length < 2) {
            // Not a valid NeoForge version string (must have at least two parts)
            return null;
        }

        final int majorVersion;
        try {
            majorVersion = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return null; // First part is not a number
        }

        if (majorVersion < 26) {
            // Legacy system (e.g., 20.2.59 for MC 1.20.2)
            // The Minecraft version is 1.major.minor
            return "1." + parts[0] + "." + parts[1];
        } else {
            // New system (e.g., 26.1.0.5 for MC 26.1.0)
            // The Minecraft version is year.release.patch
            if (parts.length < 3) {
                // Handle cases like "26.1"
                return String.join(".", parts);
            }
            // Handle cases like "26.1.0" or "26.1.0.5"
            final String[] mcParts = Arrays.copyOf(parts, 3);
            return String.join(".", mcParts);
        }
    }
}
