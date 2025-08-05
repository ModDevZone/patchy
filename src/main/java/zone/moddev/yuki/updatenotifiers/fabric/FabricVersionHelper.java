package zone.moddev.yuki.updatenotifiers.fabric;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import zone.moddev.yuki.updatenotifiers.SharedVersionHelpers;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public final class FabricVersionHelper extends SharedVersionHelpers {


    private static final String YARN_URL = "https://meta.fabricmc.net/v2/versions/yarn";
    private static final String LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader";
    private static final String API_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml";

    public static String getLatestYarn(final String mcVersion) {
        return getLatestYarnVersions().getOrDefault(mcVersion, null);
    }

    public static Map<String, String> getLatestYarnVersions() {
        final InputStreamReader reader = getReader(YARN_URL);
        if (reader == null) {
            return Map.of();
        }
        final TypeToken<List<SharedVersionHelpers.SharedVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionHelpers.SharedVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        final Map<String, List<SharedVersionHelpers.SharedVersionInfo>> map = versions.stream()
                .distinct()
                .collect(Collectors.groupingBy(it -> it.gameVersion));
        return map.keySet()
                .stream()
                .collect(Collectors.toMap(Function.identity(), it -> map.get(it).get(0).version));
    }

    @Nullable
    public static String getLatestLoader() {
        final InputStreamReader reader = getReader(LOADER_URL);
        if (reader == null) {
            return null;
        }
        final TypeToken<List<SharedVersionHelpers.LoaderVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionHelpers.LoaderVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        return versions.get(0).version;
    }

    @Nullable
    public static String getLatestApi() {
        return getLatestFromMavenMetadata(API_URL);
    }
}
