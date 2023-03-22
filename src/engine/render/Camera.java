package sekelsta.engine.render;

import sekelsta.engine.entity.Entity;
import shadowfox.math.Vector3f;

public class Camera {
    private Entity focus;
    private float distance = 16f;
    private float minDistance = 1f;
    private float maxDistance = 200f; // Don't set this higher than frustum.far
    private float zoomSpeed = 1f;

    private float pitch = (float)Math.toRadians(45);
    private float yaw = 0f;

    private Mode mode = Mode.THIRD_PERSON;

    private static enum Mode {
        FIRST_PERSON,
        THIRD_PERSON;

        public Mode next() {
            int index = (this.ordinal() + 1) % values().length;
            return values()[index];
        }
    }

    public Camera(Entity focus) {
        this.focus = focus;
    }

    public void setFocus(Entity focus) {
        this.focus = focus;
    }

    public void transform(MatrixStack matrixStack, float lerp) {
        if (mode == Mode.FIRST_PERSON) {
            Vector3f eye = focus.getEyeOffset();
            matrixStack.translate(-1 * eye.x, -1 * eye.y, -1 * eye.z);
            matrixStack.rotate(-1 * focus.getInterpolatedRoll(lerp), 0, 1, 0);
            matrixStack.rotate(-1 * focus.getInterpolatedPitch(lerp), 1, 0, 0);
            matrixStack.rotate(-1 * focus.getInterpolatedYaw(lerp), 0, 0, 1);
            matrixStack.translate(-1 * focus.getInterpolatedX(lerp), -1 * focus.getInterpolatedY(lerp), -1 * focus.getInterpolatedZ(lerp));
        }
        else {
            // NOT equivalent to matrixStack.rotate(pitch, yaw, roll);
            matrixStack.rotate(pitch, 1, 0, 0);
            matrixStack.rotate(-1 * yaw, 0, 0, 1);
            matrixStack.translate(-1 * getX(lerp), -1 * getY(lerp), -1 * getZ(lerp));
        }
    }

    public float getYaw() {
        if (mode == Mode.FIRST_PERSON) {
            return focus.getYaw();
        }
        else if (mode == Mode.THIRD_PERSON) {
            return yaw;
        }
        throw new RuntimeException("Can't get yaw for camera mode " + mode);
    }

    public void addYaw(float gain) {
        if (mode != Mode.THIRD_PERSON) {
            return;
        }
        yaw = (yaw + gain) % (float)(2 * Math.PI);
    }

    public void addPitch(float gain) {
        if (mode != Mode.THIRD_PERSON) {
            return;
        }
        pitch = (float)Math.min(Math.PI/2, Math.max(-Math.PI/2, pitch + gain));
    }

    public void zoom(double offset) {
        if (mode != Mode.THIRD_PERSON) {
            return;
        }
        distance += offset;
        distance = Math.max(minDistance, Math.min(maxDistance, distance));
    }

    public void scroll(double direction) {
        if (mode != Mode.THIRD_PERSON) {
            return;
        }
        zoom(zoomSpeed * direction);
    }

    public void nextMode() {
        mode = mode.next();
    }

    public float getX(float lerp) {
        float x = focus.getInterpolatedX(lerp);
        return x + distance * (float)(Math.cos(pitch) * Math.sin(yaw));
    }

    public float getY(float lerp) {
        float y = focus.getInterpolatedY(lerp);
        return y - distance * (float)(Math.cos(pitch) * Math.cos(yaw));
    }

    public float getZ(float lerp) {
        float z = focus.getInterpolatedZ(lerp);
        return z - distance * (float)Math.sin(-pitch);
    }
}
