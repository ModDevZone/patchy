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

package zone.moddev.patchy.updatecheckers.forge;

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
        return getVersionsByMinecraftVersion(METADATA_URL, ForgeVersion::new, ForgeVersionHelper::getMinecraftVersionFromForge);
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
