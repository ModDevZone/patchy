package zone.moddev.patchy.updatecheckers.forge;

public record ForgeVersion(String id) {

    @Override
    public String toString() {
        return id();
    }
}
