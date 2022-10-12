package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.Texture;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;

import sekelsta.game.entity.Projectile;

import sekelsta.tools.ObjParser;

public final class ProjectileRenderer extends EntityRenderer<Projectile> {
    public ProjectileRenderer() {
        mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/projectile.obj"))));
        texture = new Texture("oily.jpg");
    }
}
