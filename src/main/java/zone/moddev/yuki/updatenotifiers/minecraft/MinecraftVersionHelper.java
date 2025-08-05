package zone.moddev.yuki.updatenotifiers.minecraft;

import lombok.experimental.UtilityClass;
import zone.moddev.yuki.util.Utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

@UtilityClass
public final class MinecraftVersionHelper {
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
        try (final var reader = new InputStreamReader(URI.create(API_URL).toURL().openStream())) {
            return Utils.Gsons.GSON.fromJson(reader, PistonMeta.class);
        } catch (IOException ignored) {
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
