package sekelsta.game.render.entity;

import sekelsta.engine.Position;
import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.*;
import sekelsta.game.entity.Entity;
import sekelsta.game.render.Renderer;
import sekelsta.game.render.Textures;

public abstract class EntityRenderer {
    protected Mesh mesh;
    protected Texture texture;
    protected Texture specular = Textures.WHITE;
    protected Texture emission = Textures.BLACK;
    protected float scale = 1.0f;

    public void render(Renderer renderer, Entity entity, float lerp, MatrixStack stack) {
        float x = entity.getInterpolatedX(lerp);
        float y = entity.getInterpolatedY(lerp);
        float z = entity.getInterpolatedZ(lerp);
        stack.push();
        stack.translate(x, y, z);
        stack.scale(this.scale);

        float yaw = entity.getPosition().getInterpolatedYaw(lerp);
        float pitch = entity.getPosition().getInterpolatedPitch(lerp);
        float roll = entity.getPosition().getInterpolatedRoll(lerp);

        stack.rotate(yaw, 0f, 0f, 1f);
        stack.rotate(pitch, 1f, 0f, 0f);
        stack.rotate(roll, 0f, 1f, 0f);

        texture.bind();
        specular.bindSpecular();
        emission.bindEmission();
        mesh.render();
        stack.pop();
    }
}
