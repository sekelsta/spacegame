package sekelsta.game.render;

import java.awt.Font;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import sekelsta.engine.render.*;
import sekelsta.engine.Frustum;
import sekelsta.engine.entity.Movable;
import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.Camera;
import sekelsta.game.World;
import sekelsta.game.render.entity.*;
import sekelsta.math.*;

public class Renderer implements IFramebufferSizeListener {
    private ShaderProgram shader = ShaderProgram.load("/shaders/basic.vsh", "/shaders/basic.fsh");
    private ShaderProgram shader2D = ShaderProgram.load("/shaders/2d.vsh", "/shaders/2d.fsh");
    private Frustum frustum = new Frustum();
    private Matrix4f perspective = new Matrix4f();
    // Rotate from Y up (-Z forward) to Z up (+Y forward)
    private final Matrix4f coordinate_convert = new Matrix4f().rotate((float)(-1 * Math.PI/2), 1f, 0f, 0f);
    private final Matrix3f identity3f = new Matrix3f();
    private final Vector2f uiDimensions = new Vector2f(1, 1);

    private Vector3f lightPos = new Vector3f(0, 0, 0);
    private final float[] sunVertices = {
        // Position, normal, texture
        0.5f, 0, 0.5f, 0, -1, 0, 1, 1,
        -0.5f, 0, 0.5f, 0, -1, 0, 0, 1,
        -0.5f, 0, -0.5f, 0, -1, 0, 0, 0,
        0.5f, 0, -0.5f, 0, -1, 0, 1, 0};
    private final int[] sunFaces = {0, 1, 2, 2, 3, 0};
    private final RigidMesh sunMesh = new RigidMesh(sunVertices, sunFaces);
    private final Texture sunTexture = new Texture("sun.png");

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

        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0.005f, 0.005f, 0.005f, 1f);
    }

    public void render(float lerp, Camera camera, World world, Overlay overlay) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        if (camera != null) {
            renderWorld(lerp, camera, world);
        }
        renderOverlay(overlay);
    }

    private void renderWorld(float lerp, Camera camera, World world) {
        // Set up for three-dimensional rendering    
        shader.use();
        shader.setUniform("projection", perspective);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        // Render world
        matrixStack.push();
        // Move to camera coords
        camera.transform(matrixStack, lerp);
        Vector4f tlight = new Vector4f(lightPos);
        matrixStack.getResult().transform(tlight);
        shader.setUniform("light_pos", tlight.toVec3());

        float realLerp = lerp;
        if (world.isPaused()) {
            realLerp = 0;
        }
        for (Movable entity : world.getMobs()) {
            renderEntity(entity, realLerp, matrixStack);
        }

        // Render the sun
        matrixStack.push();
        matrixStack.translate(lightPos.x, lightPos.y, lightPos.z);
        matrixStack.scale(100, 100, 100);
        matrixStack.pushBillboard();
        Textures.TRANSPARENT.bind();
        sunTexture.bindEmission();
        sunMesh.render();
        matrixStack.pop();
        matrixStack.pop();

        matrixStack.pop();
    }

    private void renderOverlay(Overlay overlay) {
        // Set up for two-dimensional rendering
        shader2D.use();
        shader2D.setUniform("dimensions", uiDimensions);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        overlay.render(uiDimensions);
    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> void renderEntity(T entity, float lerp, MatrixStack matrixStack) {
        assert(entity != null);
        assert(entity.getType() != null);
        assert(entity.getType().getRenderer() != null);
        // Unchecked cast
        EntityRenderer<? super T> renderer = (EntityRenderer<? super T>)(entity.getType().getRenderer());
        renderer.render(entity, lerp, matrixStack);
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

        // This is the size of UI's canvas, so the scale is inversly proportional to actual element size
        // TODO: Make this scale adjustable
        float uiScale = (float)Overlay.getScale();
        uiDimensions.x = width * uiScale;
        uiDimensions.y = height * uiScale;
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
        shader.delete();
        shader2D.delete();
    }
}
