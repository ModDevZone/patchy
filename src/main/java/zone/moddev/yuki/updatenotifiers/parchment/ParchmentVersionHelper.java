package zone.moddev.yuki.updatenotifiers.parchment;

import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import zone.moddev.yuki.updatenotifiers.minecraft.MinecraftVersionHelper;
import zone.moddev.yuki.util.SemVer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class ParchmentVersionHelper {
    public static final String METADATA_URL = "https://maven.parchmentmc.org/org/parchmentmc/data/parchment-%s/maven-metadata.xml";
    public static final SemVer INITIAL_VERSION = SemVer.from("1.16.5");

    public static Map<String, String> byMcReleases() {
        final var parser = DocumentBuilderFactory.newInstance();
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) return Map.of();
        final Map<String, String> map = new HashMap<>();
        meta.versions.stream()
                .filter(it -> it.type().equals("release"))
                .map(v -> SemVer.from(v.id()))
                .filter(v -> v.compareTo(INITIAL_VERSION) >= 0)
                .forEach(v -> {
                    try (final var is = new URL(METADATA_URL.formatted(v)).openStream()) {
                        final var xml = parser.newDocumentBuilder().parse(is);
                        xml.getDocumentElement().normalize();
                        final var latestVersion = ((Element) ((Element) (xml.getElementsByTagName("metadata").item(0)))
                                .getElementsByTagName("versioning").item(0))
                                .getElementsByTagName("release").item(0)
                                .getTextContent();
                        map.put(v.toString(), latestVersion);
                    } catch (Exception ignored) {
                    }
                });
        return map;
    }

    public static ParchmentVersion newest(Map<String, String> map) {
        if (map.isEmpty()) return null;
        return map.entrySet().stream()
                .max(Comparator.comparing(it -> dateFromParchment(it.getValue())))
                .map(it -> new ParchmentVersion(it.getKey(), it.getValue()))
                .orElse(null);
    }

    public static Date dateFromParchment(String parchmentRelease) {
        final var split = parchmentRelease.split("\\.");
        // noinspection deprecation,MagicConstant
        return new Date(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public record ParchmentVersion(String mcVersion, String parchmentVersion) {
        public Date getDate() {
            return dateFromParchment(parchmentVersion());
        }
    }

}
