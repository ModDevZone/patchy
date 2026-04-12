/*
 * MIT License
 *
 * Copyright (c) 2016 - 2026 Mod Dev Zone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package zone.moddev.patchy.updatecheckers.neoforge;

import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;

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
    public static Map<String, String> getNeoForgeVersions() {
        return getVersionsByMinecraftVersion(METADATA_URL, NeoForgeVersionHelper::getMinecraftVersionFromNeoForge);
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
    public static String getMinecraftVersionFromNeoForge(String neoForgeVersion) {
        // Strip suffixes like -beta
        final String versionCore = neoForgeVersion.split("-")[0];
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
