package sekelsta.engine.render.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.tools.ModelData;
import sekelsta.tools.Vertex;

public class SkeletalMesh extends Mesh {
    private int intVBO;

    private SkeletalMesh() {
        this.intVBO = GL20.glGenBuffers();
    }

    public SkeletalMesh(ModelData data) {
        this();
        int[] faces = buildFaces(data);
        float[] vertices = buildVertices(data);

        final int size = getVertexBufferStride();
        for (int i = 0; i < data.getVertices().size(); ++i) {
            Vertex vertex = data.getVertices().get(i);
            for (int j = 0; j < ModelData.MAX_BONE_INFLUENCE; j++) {
                int index = size * i + 8 + j;
                if (vertex.boneWeights != null && j < vertex.boneWeights.length) {
                    vertices[index] = vertex.boneWeights[j];
                }
                else {
                    vertices[index] = 0;
                }
            }
        }

        int[] boneIDs = new int[ModelData.MAX_BONE_INFLUENCE * data.getVertices().size()];
        for (int i = 0; i < data.getVertices().size(); ++i) {
            for (int j = 0; j < ModelData.MAX_BONE_INFLUENCE; ++j) {
                int index = i * ModelData.MAX_BONE_INFLUENCE + j;
                int[] boneData = data.getVertices().get(i).boneIDs;
                if (boneData != null && j < boneData.length) {
                    boneIDs[index] = boneData[j];
                }
                else {
                    boneIDs[index] = -1;
                }
            }
        }
        
        init(vertices, boneIDs, faces);
    }

    @Override
    protected int getVertexBufferStride() {
        // 3D vertex, 3D normal, 2D texture, max bone weights
        return 3 + 3 + 2 + ModelData.MAX_BONE_INFLUENCE;
    }

    private void init(float[] vertices, int[] boneIDs, int[] faces) {
        bufferVertexData(vertices);
        bufferFaceElements(faces);

        // TODO: Re-use code by calling enablePositionNormalTexture()
        final int LEN = getVertexBufferStride();
        // 0 = position
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, LEN * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        // 1 = normal
        GL20.glVertexAttribPointer(1, 3, GL20.GL_FLOAT, true, LEN * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        // 2 = texture
        GL20.glVertexAttribPointer(2, 2, GL20.GL_FLOAT, false, LEN * Float.BYTES, 6 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
        // 3 = bone weights
        GL20.glVertexAttribPointer(3, ModelData.MAX_BONE_INFLUENCE, GL20.GL_FLOAT, false, LEN * Float.BYTES, 8 * Float.BYTES);
        GL20.glEnableVertexAttribArray(3);

        IntBuffer boneIndexBuffer = MemoryUtil.memAllocInt(boneIDs.length);
        boneIndexBuffer.put(boneIDs).flip();
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, intVBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, boneIndexBuffer, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(boneIndexBuffer);

        // 4 = bone IDs
        GL20.glVertexAttribPointer(4, ModelData.MAX_BONE_INFLUENCE, GL20.GL_FLOAT, false, 0, 0);
        // TODO: Check if the above needs to be replaced with
        // GL30.glVertexAttribIPointer(4, ModelData.MAX_BONE_INFLUENCE, GL20.GL_INT, 0, 0);
        GL20.glEnableVertexAttribArray(4);
    }
}
