package sekelsta.game;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.MatrixStack;

public class Camera {
    Entity focus;
    float distance = 6f; // 2f for bunny game
    float minDistance = 4f;
    float maxDistance = 200f; // TODO: Warn if greater than frustum.far
    float zoomSpeed = 1f; // 0.25 for bunny game

    // Radians
    float pitch = (float)Math.toRadians(45);
    float yaw = 0f;

    public Camera(Entity position) {
        focus = position;
    }

    public void transform(MatrixStack matrixStack, float lerp) {
        matrixStack.rotate(pitch, 1, 0, 0);
        matrixStack.rotate(-1 * yaw, 0, 0, 1);
        matrixStack.translate(-1 * getX(lerp), -1 * getY(lerp), -1 * getZ(lerp));
    }

    public float getYaw() {
        return yaw;
    }

    public void addYaw(float gain) {
        yaw = (yaw + gain) % (float)(2 * Math.PI);
    }

    public void addPitch(float gain) {
        pitch = (float)Math.min(Math.PI/2, Math.max(-Math.PI/2, pitch + gain));
    }

    public void zoom(double offset) {
        distance += offset;
        distance = Math.max(minDistance, Math.min(maxDistance, distance));
    }

    public void scroll(double direction) {
        zoom(zoomSpeed * direction);
    }

    private float getX(float lerp) {
        float x = focus.getInterpolatedX(lerp);
        return x + distance * (float)(Math.cos(pitch) * Math.sin(yaw));
    }

    private float getY(float lerp) {
        float y = focus.getInterpolatedY(lerp);
        return y - distance * (float)(Math.cos(pitch) * Math.cos(yaw));
    }

    private float getZ(float lerp) {
        float z = focus.getInterpolatedZ(lerp);
        return z - distance * (float)Math.sin(-pitch);
    }
}
