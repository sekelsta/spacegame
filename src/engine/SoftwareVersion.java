package sekelsta.engine;

import java.nio.ByteBuffer;

import sekelsta.engine.network.ByteVector;

public record SoftwareVersion (int major, int minor, int patch) {
    public boolean matchesMajorMinor(SoftwareVersion other) {
        return this.major == other.major && this.minor == other.minor;
    }

    public String toString() {
        return "" + major + "." + minor + "." + patch;
    }

    public void encode(ByteVector buffer) {
        buffer.putInt(major);
        buffer.putInt(minor);
        buffer.putInt(patch);
    }

    public static SoftwareVersion fromBuffer(ByteBuffer buffer) {
        int major = buffer.getInt();
        int minor = buffer.getInt();
        int patch = buffer.getInt();
        return new SoftwareVersion(major, minor, patch);
    }
}

