package sekelsta.engine.render;

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
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 8 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        // 1 = normal
        GL20.glVertexAttribPointer(1, 3, GL20.GL_FLOAT, true, 8 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        // 2 = texture
        GL20.glVertexAttribPointer(2, 2, GL20.GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
    }

    // Params: Screen rect, texture x and y to draw from, and texture size
    // Will render if the buffer is full, so be sure the texture is bound before calling
    public void blit(int x, int y, int width, int height, int texX, int texY, int texture_width, int texture_height) {
        // Check if we can't fit another four vertices
        if (numIndices / 6 >= size / getVertexBufferStride() / 4) {
            Log.metric("Overfilled spritebatch (size " + size + "), rendering early");
            render();
        }

        float u0 = (float)texX / (float)texture_width;
        float v0 = (float)texY / (float)texture_height;
        float u1 = (float)(texX + width) / (float)texture_width;
        float v1 = (float)(texY + height) / (float)texture_height;

        // Top-left
        vertices.put(x).put(y).put(0);
        vertices.put(0).put(0).put(1);
        vertices.put(u0).put(v0);
        // Top-right
        vertices.put(x + width).put(y).put(0);
        vertices.put(0).put(0).put(1);
        vertices.put(u1).put(v0);
        // Bottom-left
        vertices.put(x).put(y + height).put(0);
        vertices.put(0).put(0).put(1);
        vertices.put(u0).put(v1);
        // Bottom-right
        vertices.put(x + width).put(y + height).put(0);
        vertices.put(0).put(0).put(1);
        vertices.put(u1).put(v1);

        numIndices += 6;
    }



    @Override
    protected int getVertexBufferStride() {
        // 3D vertex, 3D normal, 2D texture
        return 3 + 3 + 2;
    }

    @Override
    public void render() {
        if (numIndices == 0) {
            return;
        }
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
