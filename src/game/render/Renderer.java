package sekelsta.game.render;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import sekelsta.engine.render.*;
import sekelsta.engine.Frustum;
import sekelsta.engine.entity.Movable;
import sekelsta.engine.entity.Entity;
import sekelsta.game.Camera;
import sekelsta.game.World;
import sekelsta.game.render.entity.*;
import sekelsta.math.Matrix3f;
import sekelsta.math.Matrix4f;
    // TEMP_DEBUG
import sekelsta.engine.render.text.BitmapFont;

public class Renderer implements IFramebufferSizeListener {
    private ShaderProgram shader = ShaderProgram.load("/shaders/vertex.vsh", "/shaders/fragment.fsh");
    private Frustum frustum = new Frustum();
    private Matrix4f perspective = new Matrix4f();
    // Rotate 180 deg about the axis to get y-down, x-right
    private Matrix4f orthographic = new Matrix4f().rotate((float)(Math.PI), 1f, 0f, 0f).translate(-1f, -1f, 0f);
    // Rotate from Y up (-Z forward) to Z up (+Y forward)
    private final Matrix4f coordinate_convert = new Matrix4f().rotate((float)(-1 * Math.PI/2), 1f, 0f, 0f);
    private final Matrix3f identity3f = new Matrix3f();

    private final SpriteBatch spriteBatch = new SpriteBatch();
    //private final Texture test = new Texture("test_particle.png");
    //private final Texture test = new Texture("test.png");

    // TEMP_DEBUG
    private final BitmapFont font = new BitmapFont();

    private MatrixStack matrixStack = new MatrixStack() {
        @Override
        protected void onChange() {
            Matrix4f result = getResult();
            shader.setUniform("modelview", result);
            shader.setUniform("normal_transform", normalTransform(result));
        }
    };

    public Renderer() {
        shader.use();
        shader.setUniform("texture_sampler", 0);
        shader.setUniform("specular_sampler", 1);
        shader.setUniform("emission_sampler", 2);

        frustum.setFOV(Math.toRadians(30));
    }

    public void render(float lerp, Camera camera, World world) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);        
        shader.use();
        // Set up for three-dimensional rendering
        shader.setUniform("projection", perspective);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        // Render world
        matrixStack.push();
        // Move to camera coords
        camera.transform(matrixStack, lerp);

        for (Entity entity : world.getEntities()) {
            assert(entity != null);
            assert(entity.getType() != null);
            assert(entity.getType().getRenderer() != null);
            entity.getType().getRenderer().render(entity, lerp, matrixStack);
        }
        for (Movable entity : world.getMobs()) {
            assert(entity != null);
            assert(entity.getType() != null);
            assert(entity.getType().getRenderer() != null);
            entity.getType().getRenderer().render(entity, lerp, matrixStack);
        }
        matrixStack.pop();
        // Set up for two-dimensional rendering
        shader.setUniform("projection", orthographic);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // Render UI and HUD
        // TODO
        //test.bind();
        //test.bindEmission();
        //sekelsta.game.render.Textures.TRANSPARENT.bindSpecular();
        //spriteBatch.blit(0, 0, 51, 51, 0, 0, 512, 512);
        spriteBatch.render();
    }

    public void setJointTransforms(Matrix4f[] joints) {
        for (int i = 0; i < joints.length; ++i) {
            shader.setUniform("bone_matrices[" + i + "]", joints[i]);
        }
    }

    public void windowResized(int width, int height) {
        // Ban 0 width or height
        width = Math.max(width, 1);
        height = Math.max(height, 1);

        frustum.setAspectRatio(width, height);
        frustum.calcMatrix(perspective);
        Matrix4f.mul(coordinate_convert, perspective, perspective);
        
        orthographic.setIdentity().rotate((float)(Math.PI), 1f, 0f, 0f).translate(-1f, -1f, 0f);
        orthographic.scale(2f / width, 2f / height, 1f);
    }

    private Matrix3f normalTransform(Matrix4f matrix) {
        Matrix3f n = new Matrix3f();
        n.m00 = matrix.m00;
        n.m01 = matrix.m01;
        n.m02 = matrix.m02;
        n.m10 = matrix.m10;
        n.m11 = matrix.m11;
        n.m12 = matrix.m12;
        n.m20 = matrix.m20;
        n.m21 = matrix.m21;
        n.m22 = matrix.m22;
        return (Matrix3f)(n.invert().transpose());
    }

    public void enterWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public void exitWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public void clean() {
        spriteBatch.clean();
        shader.delete();
    }
}
