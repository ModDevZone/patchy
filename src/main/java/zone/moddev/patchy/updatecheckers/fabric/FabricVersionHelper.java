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

package zone.moddev.patchy.updatecheckers.fabric;

import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;
import zone.moddev.patchy.updatecheckers.fabric.api.FabricApiVersion;

import java.util.Map;

public final class FabricVersionHelper extends SharedVersionHelpers {

    private static final String MAVEN_URL = "https://maven.fabricmc.net/net/fabricmc/";
    private static final String FABRIC_API_URL = "fabric-api/fabric-api/maven-metadata.xml";
    private static final String FABRIC_LOADER_URL = "fabric-loader/maven-metadata.xml";

    /**
     * Gets the latest version of the Fabric Loader.
     *
     * @return The latest version, or null if it could not be resolved.
     */
    @Nullable
    public static String getLatestFabricLoaderVersion() {
        return SharedVersionHelpers.getLatestFromMavenMetadata(MAVEN_URL + FABRIC_LOADER_URL);
    }

    /**
     * Gets a map of the latest Fabric API version for each Minecraft version.
     *
     * @return A map of Minecraft versions to the latest corresponding Fabric API version.
     */
    @Nullable
    public static Map<String, String> getFabricApiVersions() {
        return getVersionsByMinecraftVersion(MAVEN_URL + FABRIC_API_URL, version -> {
            final FabricApiVersion apiVersion = FabricApiVersion.fromString(version);
            return apiVersion != null ? apiVersion.mcPart() : null;
        });
    }
}
