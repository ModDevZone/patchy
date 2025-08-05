package zone.moddev.yuki.updatenotifiers;

import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;
import zone.moddev.yuki.Yuki;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SharedVersionHelpers {

    public static InputStreamReader getReader(final String urlString) {
        final InputStream stream = getStream(urlString);
        if (stream == null) {
            return null;
        } else {
            return new InputStreamReader(stream, StandardCharsets.UTF_8);
        }
    }

    @Nullable
    public static InputStream getStream(final String urlString) {
        try {
            final var url = new URL(urlString);
            return url.openStream();
        } catch (IOException ex) {
            Yuki.LOGGER.error("Failed to open input stream", ex);
            return null;
        }
    }

    public static String getLatestFromMavenMetadata(String url) {
        final InputStream stream = getStream(url);
        if (stream == null) {
            return null;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(stream);
            final XPathExpression expr = XPathFactory.newInstance()
                    .newXPath()
                    .compile("/metadata/versioning/latest/text()");
            return expr.evaluate(doc);
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            Yuki.LOGGER.error("Failed to resolve latest version from url '{}'", url, ex);
        }
        return null;
    }

    public static String replaceGitHubReferences(String changelog, String repo) {
        return changelog.replaceAll("\\(#(?<number>\\d+)\\)", "[(#$1)](https://github.com/" + repo + "/pull/$1)")
                .replaceAll("(?m)^ - ", "- ")
                .replaceAll("(?mi)(?<type>(?:close|fix|resolve)(?:s|d|es|ed)?) #(?<number>\\d+)", "$1 [#$2](https://github.com/" + repo + "/issues/$2)");
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
