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

package zone.moddev.patchy.updatecheckers.blockbench;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class BlockbenchVersionHelper {
    @Nullable
    public static GithubRelease getLatest(final Marker loggingMarker) throws IOException {
        try {
            final HttpResponse<List<GithubRelease>> response = Constants.HTTP_CLIENT.send(HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/JannisX11/blockbench/releases"))
                    .header("Accept", "application/json")
                    .build(), Constants.ofGson(Constants.GSON, new TypeToken<>() {
            }));

            if (response.statusCode() != 200) {
                Patchy.LOGGER.error(loggingMarker, "Server replied with non-200 status code {}.", response.statusCode());
                return null;
            }

            final List<GithubRelease> releases = response.body();
            return releases.isEmpty() ? null : releases.get(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
