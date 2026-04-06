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

package zone.moddev.patchy.updatecheckers.neoforge;

import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;
import zone.moddev.patchy.util.NetworkUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NeoForgeVersionHelper extends SharedVersionHelpers {
    private NeoForgeVersionHelper() {
        // Prevent instantiation
    }

    private static final String METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";

    public static Map<String, String> getNeoForgeVersions() {
        final LinkedHashMap<String, String> versions = new LinkedHashMap<>();

        final String content = NetworkUtils.getUrlContent(METADATA_URL);
        if (content == null || content.isBlank()) {
            return versions;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(content)));
            final XPathExpression expr = XPathFactory.newInstance()
                .newXPath()
                .compile("/metadata/versioning/versions/version");
            final NodeList versionsNode = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < versionsNode.getLength(); i++) {
                final String version = versionsNode.item(i).getTextContent();

                final String[] parts = version.split("-")[0].split("\\.");
                if (parts.length < 2) {
                    continue;
                }

                final String majorStr = parts[0];
                final String minorStr = parts[1];
                final int major;
                try {
                    major = Integer.parseInt(majorStr);
                } catch (NumberFormatException e) {
                    continue;
                }

                final String mcVersion;
                if (major <= 26) {
                    mcVersion = "1." + majorStr + "." + minorStr;
                } else {
                    mcVersion = majorStr + "." + minorStr;
                }
                versions.put(mcVersion, version);
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            Patchy.LOGGER.error("Failed to resolve latest version from NeoForge metadata URL", ex);
        }

        return versions;
    }
}
