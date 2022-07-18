package sekelsta.engine.render.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class RigidMesh extends Mesh {

    public RigidMesh(ModelData data) {
        float[] vertices = buildVertices(data);
        int[] faces = buildFaces(data);
        init(vertices, faces);
    }

    public RigidMesh(float[] vertices, int[] faces) {
        init(vertices, faces);
    }

    @Override
    protected int getVertexBufferStride() {
        // 3D vertex, 3D normal, 2D texture
        return 3 + 3 + 2;
    }

    private void init(float[] vertices, int[] faces) {
        bufferVertexData(vertices);
        bufferFaceElements(faces);

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
}
