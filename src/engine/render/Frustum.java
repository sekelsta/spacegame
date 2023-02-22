package sekelsta.engine.render;

import shadowfox.math.Matrix4f;

public class Frustum {
    private final float height = 0.1f; // height from middle to top, AKA half the height
    private float width = height; // distance from middle to side, AKA half width
    private float near = height;
    private float far = 5000;

    private Matrix4f matrix = null;
    private Matrix4f inverse = null;

    public float getFar() {
        return far;
    }

    public void setFar(float far) {
        this.far = far;
        this.matrix = null;
        this.inverse = null;
    }

    public void setFOV(double radians) {
        this.near = height / (float)Math.tan(radians);
        this.matrix = null;
        this.inverse = null;
    }

    public void setAspectRatio(double width, double height) {
        setAspectRatio((float)(width / height));
        this.matrix = null;
        this.inverse = null;
    }

    public void setAspectRatio(float ratio) {
        this.width = this.height * ratio;
        this.matrix = null;
        this.inverse = null;
    }

    public Matrix4f getMatrix() {
        if (this.matrix == null) {
            this.matrix = new Matrix4f().frustumY(near, far, width, height);
        }
        return this.matrix;
    }

    public Matrix4f getInverse() {
        if (this.inverse == null) {
            this.inverse = Matrix4f.invert(getMatrix(), new Matrix4f());
        }
        return this.inverse;
    }

    public Matrix4f calcMatrix(Matrix4f matrix) {
        return matrix.frustumY(near, far, width, height);
    }

    // Returns the matrix for this frustum but expanded by at least dist in each direction
    // For a Y-facing frustum
    public Matrix4f grown(Matrix4f matrix, float dist) {
        float len = Math.min(width, height);
        float t = (float)Math.sqrt(len * len + near * near) * dist / len;
        matrix.frustumY(near, far + t + dist, width, height);
        return matrix.translate(0, t, 0);
    }
}
