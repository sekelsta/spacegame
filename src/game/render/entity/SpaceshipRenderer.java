package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.MatrixStack;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Spaceship;

import sekelsta.tools.ObjParser;

public class SpaceshipRenderer extends EntityRenderer {
    private Texture[] skins = new Texture[Spaceship.NUM_SKINS];

    public SpaceshipRenderer() {
        mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/spaceship.obj"))));
        skins[0] = new Texture("gray.jpg");
        skins[1] = new Texture("red.jpg");
        skins[2] = new Texture("blue.jpg");
    }

    @Override
    public void render(Entity entity, float lerp, MatrixStack stack) {
        assert(entity instanceof Spaceship);
        Spaceship spaceship = (Spaceship)entity;
        this.texture = skins[spaceship.skin];
        super.render(entity, lerp, stack);
    }
}
