package sekelsta.engine.render.entity;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.*;

public abstract class EntityRenderer {
    protected Mesh mesh;
    protected Texture texture;
    protected Texture specular = Textures.WHITE;
    protected Texture emission = Textures.BLACK;
    protected float scale = 1.0f;

    public void render(Entity entity, float lerp, MatrixStack stack) {
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
        specular.bindSpecular();
        emission.bindEmission();
        mesh.render();
        stack.pop();
    }
}
