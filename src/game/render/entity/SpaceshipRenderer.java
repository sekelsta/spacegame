package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.MatrixStack;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Spaceship;

import sekelsta.tools.ObjParser;

public final class SpaceshipRenderer extends EntityRenderer<Spaceship> {
    private Texture[] skins = new Texture[Spaceship.NUM_SKINS];

    public SpaceshipRenderer() {
        mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/spaceship.obj"))));
        skins[0] = new Texture("gray.jpg");
        skins[1] = new Texture("red.jpg");
        skins[2] = new Texture("blue.jpg");
    }

    @Override
    public void render(Spaceship entity, float lerp, MatrixStack stack) {
        this.texture = skins[entity.skin];
        super.render(entity, lerp, stack);
    }
}
