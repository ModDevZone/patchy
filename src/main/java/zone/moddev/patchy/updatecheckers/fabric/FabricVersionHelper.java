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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;
import zone.moddev.patchy.util.NetworkUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FabricVersionHelper extends SharedVersionHelpers {

    private static final String YARN_URL = "https://meta.fabricmc.net/v2/versions/yarn";
    private static final String LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader";
    private static final String API_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml";

    public static String getLatestYarn(final String mcVersion) {
        return getYarnVersions().getOrDefault(mcVersion, null);
    }

    public static Map<String, String> getYarnVersions() {
        final String content = NetworkUtils.getUrlContent(YARN_URL);
        if (content == null || content.isBlank()) {
            return Collections.emptyMap();
        }
        final TypeToken<List<SharedVersionHelpers.SharedVersionInfo>> token = new TypeToken<>() {};
        final List<SharedVersionHelpers.SharedVersionInfo> versions = new Gson().fromJson(content, token.getType());

        final Map<String, List<SharedVersionHelpers.SharedVersionInfo>> map = versions.stream()
            .distinct()
            .collect(Collectors.groupingBy(it -> it.gameVersion));
        return map.keySet()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> map.get(it).get(0).version));
    }

    @Nullable
    public static String getLatestLoader() {
        final String content = NetworkUtils.getUrlContent(LOADER_URL);
        if (content == null || content.isBlank()) {
            return null;
        }
        final TypeToken<List<SharedVersionHelpers.LoaderVersionInfo>> token = new TypeToken<>() {};
        final List<SharedVersionHelpers.LoaderVersionInfo> versions = new Gson().fromJson(content, token.getType());

        return versions.get(0).version;
    }

    @Nullable
    public static String getLatestApi() {
        return SharedVersionHelpers.getLatestFromMavenMetadata(API_URL);
    }
}
