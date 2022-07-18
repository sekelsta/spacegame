package sekelsta.math;

public class Vector4f {
    public float x, y, z, w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(float x, float y, float z) {
        this(x, y, z, 1f);
    }

    public Vector3f toVec3() {
        return new Vector3f(x/w, y/w, z/w);
    }
}
