package sekelsta.game.render;

import java.util.*;

import org.lwjgl.opengl.GL11;

import sekelsta.engine.Particle;
import sekelsta.engine.entity.Entity;
import sekelsta.engine.render.*;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.World;
import sekelsta.game.render.gui.Overlay;
import shadowfox.math.*;
import sekelsta.tools.ObjParser;

public class Renderer implements IFramebufferSizeListener {
    private MaterialShader shader = MaterialShader.load("/shaders/basic.vsh", "/shaders/basic.fsh");
    private ShaderProgram shader2D = ShaderProgram.load("/shaders/2d.vsh", "/shaders/2d.fsh");
    private ShaderProgram fireShader = ShaderProgram.load("/shaders/fire.vsh", "/shaders/fire.fsh");
    private Frustum frustum = new Frustum();
    private Matrix4f perspective = new Matrix4f();
    // Rotate from Y up (-Z forward) to Z up (+Y forward)
    private final Matrix4f coordinate_convert = new Matrix4f().rotate((float)(-1 * Math.PI/2), 1f, 0f, 0f);
    private final Matrix3f identity3f = new Matrix3f();
    private final Vector2f uiDimensions = new Vector2f(1, 1);

    private int frameWidth;
    private int frameHeight;

    private final float[] quadVertices = {
        // Position, normal, texture
        0.5f, 0, 0.5f, 0, -1, 0, 1, 1,
        -0.5f, 0, 0.5f, 0, -1, 0, 0, 1,
        -0.5f, 0, -0.5f, 0, -1, 0, 0, 0,
        0.5f, 0, -0.5f, 0, -1, 0, 1, 0};
    private final int[] quadFaces = {0, 1, 2, 2, 3, 0};
    private final RigidMesh quadMesh = new RigidMesh(quadVertices, quadFaces);
    private final Texture sunTexture = new Texture("sun.png");
    private final Texture circleTexture = new Texture("white_circle.png");

    private final float CUBE_FACTOR = 1f / (float)Math.sqrt(3);
    private final RigidMesh skybox = new RigidMesh(
        ObjParser.parse(
            new Scanner(Renderer.class.getResourceAsStream("/assets/obj/skybox.obj"))
        )
    );
    private final Texture skyTexture = new Texture("skybox.png");

    private MatrixStack matrixStack = new MatrixStack() {
        @Override
        protected void onChange() {
            Matrix4f result = getResult();
            shader.setUniform("modelview", result);
            shader.setUniform("normal_transform", result.normalTransform());
        }
    };

    private ParticleRenderer particleRenderer = new ParticleRenderer();

    public Renderer() {
        shader.use();
        shader.setInt("texture_sampler", 0);
        shader.setInt("emission_sampler", 1);
        shader.setDefaultMaterial();

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

        if (world.isPaused()) {
            lerp = 0;
        }

        Vector3f lightPos = world.lightPos;

        // Move to camera coords
        camera.transform(matrixStack, lerp);
        Vector4f tlight = new Vector4f(lightPos);
        matrixStack.getResult().transform(tlight);
        shader.setUniform("light_pos", tlight.toVec3());

        // Render skybox
        matrixStack.push();
        matrixStack.center();
        matrixStack.scale(frustum.getFar() * CUBE_FACTOR);
        Textures.TRANSPARENT.bind();
        skyTexture.bindEmission();
        skybox.render();
        matrixStack.pop();

        // Render entities
        for (Entity entity : world.getMobs()) {
            renderEntity(entity, lerp, matrixStack, shader);
            shader.setDefaultMaterial();
        }

        // Render particles
        List<Particle> particles = world.getParticles();

        fireShader.use();
        fireShader.setUniform("projection", perspective);
        circleTexture.bind();
        particleRenderer.render(camera, matrixStack, particles, fireShader, 0.3f, lerp);
        shader.use();

        // Render the sun
        matrixStack.push();
        matrixStack.translate(lightPos.x, lightPos.y, lightPos.z);
        matrixStack.scale(2 * world.sunRadius);
        matrixStack.billboard();
        Textures.TRANSPARENT.bind();
        sunTexture.bindEmission();
        quadMesh.render();
        matrixStack.pop();

        matrixStack.pop();
    }

    private void renderOverlay(Overlay overlay) {
        // Set up for two-dimensional rendering
        shader2D.use();

        // This is the size of UI's canvas, so the scale is inversly proportional to actual element size
        float uiScale = overlay.getScale();
        uiDimensions.x = frameWidth * uiScale;
        uiDimensions.y = frameHeight * uiScale;
        shader2D.setUniform("dimensions", uiDimensions);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        overlay.render(uiDimensions);
    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> void renderEntity(T entity, float lerp, MatrixStack matrixStack, MaterialShader shader) {
        assert(entity != null);
        assert(entity.getType() != null);
        assert(entity.getType().getRenderer() != null);
        // Unchecked cast
        EntityRenderer<? super T> renderer = (EntityRenderer<? super T>)(entity.getType().getRenderer());
        renderer.render(entity, lerp, matrixStack, shader);
    }

    public void setJointTransforms(Matrix4f[] joints) {
        for (int i = 0; i < joints.length; ++i) {
            shader.setUniform("bone_matrices[" + i + "]", joints[i]);
        }
    }

    @Override
    public void windowResized(int width, int height) {
        // Ban 0 width or height
        frameWidth = Math.max(width, 1);
        frameHeight = Math.max(height, 1);

        frustum.setAspectRatio(frameWidth, frameHeight);
        frustum.calcMatrix(perspective);
        Matrix4f.mul(coordinate_convert, perspective, perspective);
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
        fireShader.delete();
        particleRenderer.clean();
    }
}
