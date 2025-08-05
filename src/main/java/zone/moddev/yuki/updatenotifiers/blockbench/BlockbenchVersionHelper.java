package zone.moddev.yuki.updatenotifiers.blockbench;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import zone.moddev.yuki.Yuki;
import zone.moddev.yuki.util.StringSerializer;
import zone.moddev.yuki.util.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class BlockbenchVersionHelper {

    @Nullable
    public static GithubRelease getLatest(final Marker loggingMarker) throws IOException {
        final HttpResponse<List<GithubRelease>> response;
        try {
            response = Utils.HTTP_CLIENT.send(HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/JannisX11/blockbench/releases"))
                    .header("Accept", "application/json")
                    .build(), Utils.ofGson(StringSerializer.RECORD_GSON, new TypeToken<>() {}));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            Yuki.LOGGER.error(loggingMarker, "Server replied with non-200 status code {}.", response.statusCode());
            return null;
        }

        final List<GithubRelease> releases = response.body();
        return releases.isEmpty() ? null : releases.get(0); // First is the latest
    }
}
