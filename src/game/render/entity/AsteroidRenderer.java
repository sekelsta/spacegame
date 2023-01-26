package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.*;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Asteroid;

import sekelsta.tools.ObjParser;

public final class AsteroidRenderer extends EntityRenderer<Asteroid> {
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
    public void render(Asteroid entity, float lerp, MatrixStack stack, MaterialShader shader) {
        shader.setReflectance(0.05f);
        this.mesh = mesh_variants[entity.getMeshVariant()];
        this.scale = entity.getSizeScale();
        super.render(entity, lerp, stack, shader);
    }
}
