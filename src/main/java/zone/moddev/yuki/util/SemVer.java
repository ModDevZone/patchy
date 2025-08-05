package zone.moddev.yuki.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SemVer(int major, int minor, @Nullable Integer patch) implements Comparable<SemVer> {

    public static SemVer from(final String versionString) {
        final String[] vs = versionString.split("\\.");
        final var major = Integer.parseInt(vs[0]);
        final var minor = Integer.parseInt(vs[1]);

        Integer patch = null;
        if (vs.length == 3) {
            patch = Integer.parseInt(vs[2]);
        }
        return new SemVer(major, minor, patch);
    }

    @Override
    public int compareTo(@NotNull final SemVer other) {
        if (this.major > other.major) {
            return 1;
        }
        if (this.major < other.major) {
            return -1;
        }


        if (this.minor > other.minor) {
            return 1;
        }
        if (this.minor < other.minor) {
            return -1;
        }

        if (this.patch == null && other.patch != null) {
            return -1;
        }
        if (this.patch != null && other.patch == null) {
            return 1;
        }

        if (patch == null) return 0;

        return this.patch.compareTo(other.patch);
    }

    @Override
    public String toString() {
        if (this.patch == null) {
            return String.format("%d.%d", major, minor);
        } else {
            return String.format("%d.%d.%d", major, minor, patch);
        }
    }
}
