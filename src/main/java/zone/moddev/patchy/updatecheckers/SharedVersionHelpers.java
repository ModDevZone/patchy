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

import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.util.NetworkUtils;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

public class SharedVersionHelpers {

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

    public static String replaceGitHubReferences(String changelog, String repo) {
        return changelog.replaceAll("\\(#(?<number>\\d+)\\)", "[(#$1)](https://github.com/" + repo + "/pull/$1)")
            .replaceAll("(?m)^ - ", "- ")
            .replaceAll("(?mi)(?<type>(?:close|fix|resolve)(?:s|d|es|ed)?) #(?<number>\\d+)", "$1 [#$2](https://github.com/" + repo + "/issues/$2)");
    }

    public static String truncate(final String str, int limit) {
        return str.length() > (limit - 3) ? str.substring(0, limit - 3) + "..." : str;
    }

    public static class SharedVersionInfo {
        public String gameVersion;
        public int build;
        public String version;
    }

    public static class LoaderVersionInfo {
        public int build;
        public String version;
    }
}
