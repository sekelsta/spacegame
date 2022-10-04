package sekelsta.engine;

import java.nio.ByteBuffer;

public record SoftwareVersion (int major, int minor, int patch) {
    public String toString() {
        return "" + major + "." + minor + "." + patch;
    }
}

