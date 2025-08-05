package zone.moddev.yuki.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.matyrobbrt.curseforgeapi.util.gson.RecordTypeAdapterFactory;
import zone.moddev.yuki.util.gson.InstantTypeAdapter;
import zone.moddev.yuki.util.gson.PatternTypeAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.regex.Pattern;

public class Utils {

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static String truncate(final String str, int limit) {
        return str.length() > (limit - 3) ? str.substring(0, limit - 3) + "..." : str;
    }

    public static String getUrlAsString(URL u) throws IOException {
        try (final var in = u.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Thread setThreadDaemon(final Thread thread, final boolean isDaemon) {
        thread.setDaemon(isDaemon);
        return thread;
    }

    public static <T> HttpResponse.BodyHandler<T> ofGson(Gson gson, TypeToken<T> token) {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofInputStream(),
                io.github.matyrobbrt.curseforgeapi.util.Utils.rethrowFunction(stream -> {
                    final InputStreamReader reader = new InputStreamReader(stream);
                    return gson.fromJson(reader, token);
                })
        );
    }

    public static final class Gsons {
        public static final Gson NO_PRETTY_PRINTING = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                .disableHtmlEscaping()
                .setLenient()
                .create();
        public static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }
}
