package zone.moddev.yuki.updatenotifiers.forge;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Objects;

public final class ForgeVersion {

    private String recommended;
    private String latest;

    public ForgeVersion() {
        this.recommended = "(unspecified)";
        this.latest = "(unspecified)";
    }

    public String getRecommended() {
        return recommended;
    }

    @CanIgnoreReturnValue
    public ForgeVersion setRecommended(final String recommendedIn) {
        this.recommended = recommendedIn;
        return this;
    }

    public String getLatest() {
        return latest;
    }

    @CanIgnoreReturnValue
    public ForgeVersion setLatest(final String latestIn) {
        this.latest = latestIn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForgeVersion that = (ForgeVersion) o;
        return Objects.equals(recommended, that.recommended) && Objects.equals(latest, that.latest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommended, latest);
    }

    @Override
    public String toString() {
        return "ForgeVersion[recommended=%s, latest=%s]".formatted(recommended, latest);
    }
}
