package zone.moddev.yuki.updatenotifiers.neoforge;

import lombok.experimental.UtilityClass;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import zone.moddev.yuki.Yuki;
import zone.moddev.yuki.updatenotifiers.SharedVersionHelpers;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public final class NeoForgeVersionHelper extends SharedVersionHelpers {
    private static final String METADATA_URL = "https://maven.neoforged.net/net/neoforged/neoforge/maven-metadata.xml";

    public static Map<String, String> getNeoForgeVersions() {
        final LinkedHashMap<String, String> versions = new LinkedHashMap<>();

        final InputStream stream = getStream(METADATA_URL);
        if (stream == null) {
            return versions;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(stream);
            final XPathExpression expr = XPathFactory.newInstance()
                    .newXPath()
                    .compile("/metadata/versioning/versions/version");
            final NodeList versionsNode = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < versionsNode.getLength(); i++) {
                final String version = versionsNode.item(i).getTextContent();

                final String[] split = version.split("\\.", 3);
                final String mcVersion = split[0] + "." + split[1];
                versions.put("1." + mcVersion, version);
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            Yuki.LOGGER.error("Failed to resolve latest version from NeoForge metadata URL", ex);
        }

        return versions;
    }
}
