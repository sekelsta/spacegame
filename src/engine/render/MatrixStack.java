package sekelsta.engine.render;

import java.util.ArrayList;
import sekelsta.math.Matrix3f;
import sekelsta.math.Matrix4f;

public class MatrixStack {
    private ArrayList<Matrix4f> stack = new ArrayList<>();

    public void push() {
        stack.add(new Matrix4f());
        onChange();
    }

    public void pushBillboard() {
        Matrix3f rotationScale = getResult().getRotation();
        // Rotation matrices are orthogonal; transpose to invert
        rotationScale.transpose();
        stack.add(new Matrix4f(rotationScale));
        onChange();
    }

    public void pop() {
        stack.remove(topIndex());
        onChange();
    }

    public void translate(float x, float y, float z) {
        stack.get(topIndex()).translate(x, y, z);
        onChange();
    }

    public void rotate(float angle, float x, float y, float z) {
        stack.get(topIndex()).rotate(angle, x, y, z);
        onChange();
    }

    public void rotate(float yaw, float pitch, float roll) {
        stack.get(topIndex()).rotate(yaw, pitch, roll);
        onChange();
    }

    public void scale(float x, float y, float z) {
        stack.get(topIndex()).scale(x, y, z);
        onChange();
    }

    public void scale(float s) {
        this.scale(s, s, s);
    }

    private int topIndex() {
        return stack.size() - 1;
    }

    public Matrix4f getResult() {
        Matrix4f result = new Matrix4f();
        for (Matrix4f matrix : stack) {
            result.multiply(matrix);
        }
        return result;
    }

    protected void onChange() {}
}
