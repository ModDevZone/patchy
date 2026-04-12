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

package zone.moddev.patchy.updatecheckers;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.util.NetworkUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SharedVersionHelpers {

    /**
     * Gets the latest version from a maven-metadata.xml file.
     *
     * @param url The URL of the maven-metadata.xml file.
     * @return The latest version, or null if it could not be resolved.
     */
    @Nullable
    public static String getLatestFromMavenMetadata(String url) {
        String content = NetworkUtils.getUrlContent(url);
        if (content == null) {
            return null;
        }

        try {
            final var doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(content)));
            final XPathExpression expr = XPathFactory.newInstance()
                    .newXPath()
                    .compile("/metadata/versioning/latest/text()");
            return expr.evaluate(doc);
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            Patchy.LOGGER.error("Failed to resolve latest version from url '{}'", url, ex);
        }
        return null;
    }

    /**
     * Gets all versions from a maven-metadata.xml file.
     *
     * @param url The URL of the maven-metadata.xml file.
     * @return An array of all versions, or null if they could not be resolved.
     */
    @Nullable
    public static String[] getVersionsFromMavenMetadata(String url) {
        String content = NetworkUtils.getUrlContent(url);
        if (content == null) {
            return null;
        }

        try {
            final var doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(content)));
            final XPathExpression expr = XPathFactory.newInstance()
                    .newXPath()
                    .compile("/metadata/versioning/versions/version");
            final NodeList versionsNode = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            final String[] versions = new String[versionsNode.getLength()];
            for (int i = 0; i < versionsNode.getLength(); i++) {
                versions[i] = versionsNode.item(i).getTextContent();
            }
            return versions;
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            Patchy.LOGGER.error("Failed to resolve versions from url '{}'", url, ex);
        }
        return null;
    }

    /**
     * Gets a map of the latest version for each Minecraft version from a maven-metadata.xml file.
     *
     * @param url                The URL of the maven-metadata.xml file.
     * @param mcVersionExtractor A function that extracts the Minecraft version from a version string.
     * @return A map of Minecraft versions to the latest corresponding version.
     */
    public static Map<String, String> getVersionsByMinecraftVersion(String url, Function<String, String> mcVersionExtractor) {
        final String[] versions = getVersionsFromMavenMetadata(url);
        if (versions == null) {
            return Collections.emptyMap();
        }
        return Arrays.stream(versions)
                .map(v -> new AbstractMap.SimpleImmutableEntry<>(mcVersionExtractor.apply(v), v))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b, // Keep the last one found for a given MC version
                        LinkedHashMap::new // Preserve insertion order
                ));
    }

    public static String replaceGitHubReferences(String changelog, String repo) {
        return changelog.replaceAll("\\(#(?<number>\\d+)\\)", "[(#$1)](https://github.com/" + repo + "/pull/$1)")
                .replaceAll("(?m)^ - ", "- ")
                .replaceAll("(?mi)(?<type>(?:close|fix|resolve)(?:s|d|es|ed)?) #(?<number>\\d+)", "$1 [#$2](https://github.com/" + repo + "/issues/$2)");
    }

    public static String truncate(final String str, int limit) {
        return str.length() > (limit - 3) ? str.substring(0, limit - 3) + "..." : str;
    }
}
