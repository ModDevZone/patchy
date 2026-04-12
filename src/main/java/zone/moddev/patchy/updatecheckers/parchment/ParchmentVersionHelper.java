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

package zone.moddev.patchy.updatecheckers.parchment;

import org.w3c.dom.Element;
import zone.moddev.patchy.updatecheckers.minecraft.MinecraftVersionHelper;
import zone.moddev.patchy.util.SemVer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class ParchmentVersionHelper {

    private ParchmentVersionHelper() {
        // Prevent instantiation
    }

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
                    try (final var is = new URI(METADATA_URL.formatted(v)).toURL().openStream()) {
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

    public static LocalDate dateFromParchment(String parchmentRelease) {
        final var split = parchmentRelease.split("\\.");
        // The format is YYYY.MM.DD
        final int year = Integer.parseInt(split[0]);
        final int month = Integer.parseInt(split[1]);
        final int day = Integer.parseInt(split[2]);
        return LocalDate.of(year, month, day);
    }

    public record ParchmentVersion(String mcVersion, String parchmentVersion) {
        public LocalDate getDate() {
            return dateFromParchment(parchmentVersion());
        }
    }
}
