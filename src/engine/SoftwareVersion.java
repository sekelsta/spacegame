package sekelsta.engine;

import java.nio.ByteBuffer;

import sekelsta.engine.network.ByteVector;

public record SoftwareVersion (int major, int minor, int patch) {
    public String toString() {
        return "" + major + "." + minor + "." + patch;
    }
}

