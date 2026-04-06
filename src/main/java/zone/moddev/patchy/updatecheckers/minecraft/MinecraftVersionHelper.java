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

package zone.moddev.patchy.updatecheckers.minecraft;

import zone.moddev.patchy.util.Constants;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public final class MinecraftVersionHelper {
    
    private MinecraftVersionHelper() {
        // Prevent instantiation
    }

    public static final String API_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    @Nullable
    public static String getLatest() {
        final var meta = getMeta();
        if (meta == null) return null;
        return meta.latest.snapshot;
    }

    @Nullable
    public static String getLatestStable() {
        final var meta = getMeta();
        if (meta == null) return null;
        return meta.latest.release;
    }

    @Nullable
    public static PistonMeta getMeta() {
        try (final var reader = new InputStreamReader(new URI(API_URL).toURL().openStream())) {
            return Constants.GSON.fromJson(reader, PistonMeta.class);
        } catch (IOException | URISyntaxException ignored) {
            return null;
        }
    }

    public static class PistonMeta {
        public VersionsInfo latest;
        public List<VersionInfo> versions;
    }

    public record VersionsInfo(String release, String snapshot) {
    }

    public record VersionInfo(String id, String type) {
    }
}
