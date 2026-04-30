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

import com.google.gson.reflect.TypeToken;
import com.unascribed.flexver.FlexVerComparator;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;
import zone.moddev.patchy.updatecheckers.fabric.api.FabricApiVersion;
import zone.moddev.patchy.updatecheckers.fabric.loader.FabricLoaderVersion;
import zone.moddev.patchy.util.Constants;
import zone.moddev.patchy.util.NetworkUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FabricVersionHelper extends SharedVersionHelpers {

    private static final String BASE_META_URL = "https://meta.fabricmc.net";
    private static final String LOADER_VERSIONS_ENDPOINT = "/v2/versions/loader";

    // fabric api is not on meta, have to parse from maven
    private static final String MAVEN_URL = "https://maven.fabricmc.net/net/fabricmc";
    private static final String FABRIC_API_URL = "/fabric-api/fabric-api/maven-metadata.xml";

    /**
     * Gets a map of the latest Fabric API version for each Minecraft version.
     *
     * @return A map of Minecraft versions to the latest corresponding Fabric API version.
     */
    public static Map<String, FabricApiVersion> getFabricApiVersions() throws IOException {
        return getVersionsByMinecraftVersion(MAVEN_URL + FABRIC_API_URL, FabricApiVersion::fromString, FabricApiVersion::mcPart);
    }

    public static List<FabricLoaderVersion> getLoaderVersions() {
        var json = NetworkUtils.getUrlContent(BASE_META_URL + LOADER_VERSIONS_ENDPOINT);
        return json != null ? Constants.GSON.fromJson(json, new TypeToken<>() {}) : List.of();
    }

    public static Optional<FabricLoaderVersion> getLatestLoaderVersion(boolean includeBeta) {
        var stream = getLoaderVersions().stream();
        if(!includeBeta) {
            stream = stream.filter(FabricLoaderVersion::stable);
        }
        return stream.max(Comparator.comparing(FabricLoaderVersion::version, FlexVerComparator::compare));
    }
}
