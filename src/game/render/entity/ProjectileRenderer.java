package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.Texture;
import sekelsta.engine.render.mesh.RigidMesh;

import sekelsta.tools.ObjParser;

public class ProjectileRenderer extends EntityRenderer {
    public ProjectileRenderer() {
        mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/projectile.obj"))));
        texture = new Texture("oily.jpg");
    }
}
