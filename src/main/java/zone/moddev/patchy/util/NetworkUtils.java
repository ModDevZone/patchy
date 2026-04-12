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

package zone.moddev.patchy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class NetworkUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("Patchy Network Utils");

    private NetworkUtils() {
        // Utility class
    }

    /**
     * Checks if a given URL is valid and reachable (returns HTTP 200 OK).
     * Logs errors for unreachable URLs or non-200 status codes.
     *
     * @param urlString The URL to validate.
     * @return true if the URL is valid and returns HTTP 200 OK, false otherwise.
     */
    public static boolean isValidUrl(String urlString) {
        try {
            final URL url = new URI(urlString).toURL();
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);    // 5 seconds
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                LOGGER.warn("URL validation failed for {}: Received HTTP status code {}", urlString, responseCode);
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("URL validation failed for {}: {}", urlString, e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the content of a given URL as a String.
     * Logs errors for unreachable URLs or non-200 status codes.
     *
     * @param urlString The URL to fetch content from.
     * @return The content of the URL as a String if successful and HTTP 200 OK, null otherwise.
     */
    public static String getUrlContent(String urlString) {
        try {
            final URL url = new URI(urlString).toURL();
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // Use GET to fetch content
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);    // 5 seconds
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (final var in = connection.getInputStream()) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                LOGGER.warn("Failed to get URL content for {}: Received HTTP status code {}", urlString, responseCode);
                return null;
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Failed to get URL content for {}: {}", urlString, e.getMessage());
            return null;
        }
    }
}
