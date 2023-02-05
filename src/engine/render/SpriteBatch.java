package sekelsta.engine.render;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.Log;
import sekelsta.engine.render.mesh.Mesh;

// For drawing a batch of rects using the same texture
public class SpriteBatch extends Mesh {
    private FloatBuffer vertices;
    private final int size = 65536;

    private Texture texture;

    public SpriteBatch() {
        vertices = MemoryUtil.memAllocFloat(size);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, VBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, size * Float.BYTES, GL20.GL_DYNAMIC_DRAW);

        // 4 vertices per rectangle, 6 indices per rectangle
        int maxIndices = (int)(size / 4) * 6;
        IntBuffer indices = MemoryUtil.memAllocInt(maxIndices);
        for (int i = 0; i < size / 4; ++i) {
            // 0 <- 1
            // |  / ^
            // v /  |
            // 2 -> 3
            indices.put(4 * i + 1);
            indices.put(4 * i + 0);
            indices.put(4 * i + 2);
            indices.put(4 * i + 2);
            indices.put(4 * i + 3);
            indices.put(4 * i + 1);
        }
        indices.flip();

        GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indices, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(indices);

        // First argument depends on the layout value in the vertex shader
        // 0 = vertex
        GL20.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, getVertexBufferStride() * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        // 1 = texture
        GL20.glVertexAttribPointer(1, 2, GL20.GL_FLOAT, false, getVertexBufferStride() * Float.BYTES, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        // 2 = color
        GL20.glVertexAttribPointer(2, 3, GL20.GL_FLOAT, false, getVertexBufferStride() * Float.BYTES, 4 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
    }

    @Override
    protected int getVertexBufferStride() {
        // 2D vertex, 2D texture, RGB color
        return 2 + 2 + 3;
    }

    // Call this before the first blit, then after each render.
    public void setTexture(Texture texture) {
        if (this.texture == texture) {
            return;
        }
        assert(isEmpty());
        this.texture = texture;
    }

    public void blit(int x, int y, int width, int height, int texX, int texY) {
        blit(x, y, width, height, texX, texY, 1f, 1f, 1f);
    }

    public void blit(int x, int y, int width, int height, int texX, int texY, Color color) {
        blit(x, y, width, height, texX, texY, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    // Params: Screen rect, texture x and y to draw from, texture size, and color to draw
    // Will render if the buffer is full, so be sure the texture is bound before calling
    public void blit(int x, int y, int width, int height, int texX, int texY, float red, float green, float blue) {
        blitStretched(x, y, width, height, texX, texY, width, height, red, green, blue);
    }

    public void blitStretched(int x, int y, int width, int height, int texX, int texY, int texWidth, int texHeight) {
        blitStretched(x, y, width, height, texX, texY, texWidth, texHeight, 1f, 1f, 1f);
    }

    public void blitStretched(int x, int y, int width, int height, int texX, int texY, int texWidth, int texHeight,
            float red, float green, float blue) {
        assert(texture != null);

        // Check if we can't fit another four vertices
        if (numIndices / 6 >= size / getVertexBufferStride() / 4) {
            Log.metric("Overfilled spritebatch (size " + size + "), rendering early");
            render();
        }

        float u0 = (float)texX / (float)texture.getWidth();
        float v0 = (float)texY / (float)texture.getHeight();
        float u1 = (float)(texX + texWidth) / (float)texture.getWidth();
        float v1 = (float)(texY + texHeight) / (float)texture.getHeight();

        // Top-left
        vertices.put(x).put(-y);
        vertices.put(u0).put(v0);
        vertices.put(red).put(green).put(blue);
        // Top-right
        vertices.put(x + width).put(-y);
        vertices.put(u1).put(v0);
        vertices.put(red).put(green).put(blue);
        // Bottom-left
        vertices.put(x).put(-y - height);
        vertices.put(u0).put(v1);
        vertices.put(red).put(green).put(blue);
        // Bottom-right
        vertices.put(x + width).put(-y - height);
        vertices.put(u1).put(v1);
        vertices.put(red).put(green).put(blue);

        numIndices += 6;
    }

    @Override
    public void render() {
        if (numIndices == 0) {
            return;
        }
        texture.bind();
        vertices.flip();
        GL30.glBindVertexArray(VAO);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, VBO);
        GL20.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, vertices);
        GL20.glDrawElements(GL20.GL_TRIANGLES, numIndices, GL20.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
        vertices.clear();
        numIndices = 0;
    }

    public boolean isEmpty() {
        return numIndices == 0;
    }

    @Override
    public void clean() {
        MemoryUtil.memFree(vertices);
        super.clean();
    }
}
