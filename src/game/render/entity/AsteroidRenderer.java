package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.MatrixStack;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Asteroid;
import sekelsta.game.render.Renderer;

import sekelsta.tools.ObjParser;

public class AsteroidRenderer extends EntityRenderer {
    private RigidMesh[] mesh_variants = new RigidMesh[Asteroid.NUM_MESH_VARIANTS];

    public AsteroidRenderer() {
        for (int i = 0; i < mesh_variants.length; ++i) {
            mesh_variants[i] = new RigidMesh(
                ObjParser.parse(
                    new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/rock" + (i + 1) + ".obj"))
                )
            );
        }
        texture = new Texture("asteroid.jpg");
    }

    @Override
    public void render(Renderer renderer, Entity entity, float lerp, MatrixStack stack) {
        assert(entity instanceof Asteroid);
        Asteroid asteroid = (Asteroid)entity;
        this.mesh = mesh_variants[asteroid.getMeshVariant()];
        this.scale = asteroid.getSizeScale();
        super.render(renderer, entity, lerp, stack);
    }
}
