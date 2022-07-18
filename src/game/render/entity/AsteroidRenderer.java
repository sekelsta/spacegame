package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.MatrixStack;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Asteroid;
import sekelsta.game.entity.Entity;
import sekelsta.game.render.Renderer;

import sekelsta.tools.ObjParser;

public class AsteroidRenderer extends EntityRenderer {
    public AsteroidRenderer() {
        mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/rock_c_01.obj"))));
        texture = new Texture("asteroid.jpg");
    }

    @Override
    public void render(Renderer renderer, Entity entity, float lerp, MatrixStack stack) {
        assert(entity instanceof Asteroid);
        this.scale = ((Asteroid)entity).getSize();
        super.render(renderer, entity, lerp, stack);
    }
}
