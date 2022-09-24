package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.Texture;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;

import sekelsta.tools.ObjParser;

public class SpaceshipRenderer extends EntityRenderer {
    public SpaceshipRenderer() {
        mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/spaceship.obj"))));
        texture = new Texture("gray.jpg");
    }
}
