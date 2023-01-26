package sekelsta.engine.render.entity;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.*;

public abstract class EntityRenderer<T extends Entity> {
    protected Mesh mesh;
    protected Texture texture;
    protected Texture emission = Textures.BLACK;
    protected float scale = 1.0f;

    public void render(T entity, float lerp, MatrixStack stack, MaterialShader shader) {
        float x = entity.getInterpolatedX(lerp);
        float y = entity.getInterpolatedY(lerp);
        float z = entity.getInterpolatedZ(lerp);
        stack.push();
        stack.translate(x, y, z);
        stack.scale(this.scale);

        float yaw = entity.getInterpolatedYaw(lerp);
        float pitch = entity.getInterpolatedPitch(lerp);
        float roll = entity.getInterpolatedRoll(lerp);

        stack.rotate(yaw, pitch, roll);

        texture.bind();
        emission.bindEmission();
        mesh.render();
        stack.pop();
    }
}
