package sekelsta.game.render;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.Particle;
import sekelsta.engine.render.*;
import shadowfox.math.Matrix4f;

public class ParticleRenderer {
    private int quadVAO;
    private int quadVBO;
    private int particleVBO;

    public ParticleRenderer() {
        // TO_OPTIMIZE: Make it so the quad mesh can be shared with other uses
        // Set up particle data
        float[] quadVertices = {
            // Position, UV
            0.5f, 0, 0.5f, 1, 1,
            -0.5f, 0, 0.5f, 0, 1,
            -0.5f, 0, -0.5f, 0, 0,
            -0.5f, 0, -0.5f, 0, 0,
            0.5f, 0, -0.5f, 1, 0,
            0.5f, 0, 0.5f, 1, 1
        };
        // Convert to off-heap memory
        FloatBuffer quadBuffer = MemoryUtil.memAllocFloat(quadVertices.length);
        quadBuffer.put(quadVertices).flip();

        particleVBO = GL20.glGenBuffers();
        quadVAO = GL30.glGenVertexArrays();
        quadVBO = GL20.glGenBuffers();

        GL30.glBindVertexArray(quadVAO);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, quadVBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, quadBuffer, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(quadBuffer);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL20.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        // These attributes come from a different buffer
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, particleVBO);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL20.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 1, GL20.GL_FLOAT, false, 4 * Float.BYTES, 3 * Float.BYTES);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        // Specify both instanced attributes as per-object
        GL33.glVertexAttribDivisor(2, 1);
        GL33.glVertexAttribDivisor(3, 1);
    }

    public void render(Camera camera, MatrixStack matrixStack, List<Particle> particles, ShaderProgram particleShader,
            float scale, float lerp) {
        // In order to make the GPU-side computation more numerically stable,
        // we move the particles with the camera back to the origin
        float cx = camera.getX(lerp);
        float cy = camera.getY(lerp);
        float cz = camera.getZ(lerp);
        float[] particleData = new float[4 * particles.size()];
        for (int i = 0; i < particles.size(); ++i) {
            Particle particle = particles.get(i);
            particleData[4*i] = particle.getInterpolatedX(lerp) - cx;
            particleData[4*i + 1] = particle.getInterpolatedY(lerp) - cy;
            particleData[4*i + 2] = particle.getInterpolatedZ(lerp) - cz;
            particleData[4*i + 3] = particle.getRelativeAge(lerp);
        }

        // Convert to off-heap memory
        FloatBuffer particleBuffer = MemoryUtil.memAllocFloat(particleData.length);
        particleBuffer.put(particleData).flip();
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, particleVBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, particleBuffer, GL20.GL_DYNAMIC_DRAW);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(particleBuffer);

        matrixStack.push();
        matrixStack.translate(cx, cy, cz);
        Matrix4f result = matrixStack.getResult();
        particleShader.setUniform("modelview", matrixStack.getResult());
        Matrix4f billboard = new Matrix4f(result.getRotation().transpose());
        billboard.scale(scale);
        particleShader.setUniform("billboard", billboard);

        GL30.glBindVertexArray(quadVAO);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, 6, particles.size()); // Draw all particles, 6 vertices each
        GL30.glBindVertexArray(0);
        matrixStack.pop();
    }

    public void clean() {
        GL30.glDeleteVertexArrays(quadVAO);
        GL20.glDeleteBuffers(quadVBO);
        GL20.glDeleteBuffers(particleVBO);
    }
}
