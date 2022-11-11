package sekelsta.game.render.entity;

import java.awt.Color;
import java.util.Scanner;

import sekelsta.engine.render.MatrixStack;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.Textures;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;

import sekelsta.game.entity.Projectile;

import sekelsta.tools.ObjParser;

public final class ProjectileRenderer extends EntityRenderer<Projectile> {
    protected RigidMesh border;
    protected RigidMesh center;
    protected Texture glow;

    public ProjectileRenderer() {
        center = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/projectile.obj"))));
        border = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/projectile_border.obj"))));
        glow = new Texture(new Color(0x6c, 0xe0, 0xff));
        texture = Textures.BLACK;
        specular = Textures.BLACK;
    }

    @Override
    public void render(Projectile entity, float lerp, MatrixStack stack) {
        mesh = border;
        emission = glow;
        super.render(entity, lerp, stack);
        mesh = center;
        emission = Textures.BLACK;
        super.render(entity, lerp, stack);
    }
}
