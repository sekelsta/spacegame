package sekelsta.game.render.entity;

import java.awt.Color;
import java.util.Scanner;

import sekelsta.engine.render.*;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Spaceship;

import sekelsta.tools.ObjParser;

public final class SpaceshipRenderer extends EntityRenderer<Spaceship> {
    private Texture[] skins = new Texture[Spaceship.NUM_SKINS];
    private RigidMesh ship = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/spaceship.obj"))));
    private RigidMesh window = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/spaceship_window.obj"))));
    private Texture windowTexture = new Texture(new Color(0.04f, 0.06f, 0.05f, 0.95f));

    public SpaceshipRenderer() {
        skins[0] = new Texture("gray.jpg");
        skins[1] = new Texture("red.jpg");
        skins[2] = new Texture("blue.jpg");
    }

    @Override
    public void render(Spaceship entity, float lerp, MatrixStack stack, MaterialShader shader) {
        this.mesh = ship;
        this.texture = skins[entity.skin];
        super.render(entity, lerp, stack, shader);
        shader.setShininess(64);
        shader.setReflectance(1);
        this.mesh = window;
        this.texture = windowTexture;
        super.render(entity, lerp, stack, shader);
    }
}
