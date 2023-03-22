package sekelsta.engine.render.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.tools.ModelData;
import sekelsta.tools.Vertex;

public abstract class Mesh {
    protected int VAO;
    protected int VBO;
    protected int EBO;
    protected int numIndices;

    protected Mesh() {
        // vertex array object
        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);
        // Vertex Buffer Object
        this.VBO = GL20.glGenBuffers();
        // Element Buffer Object
        this.EBO = GL20.glGenBuffers();
        
    }

    protected abstract int getVertexBufferStride();

    protected float[] buildVertices(ModelData obj) {
        return buildVertices(obj.getVertices(), getVertexBufferStride());
    }

    public static float[] buildVertices(List<Vertex> vertexList, int stride) {
        float[] vertices = new float[stride * vertexList.size()];
        for (int i = 0; i < vertexList.size(); ++i) {
            Vertex data = vertexList.get(i);
            vertices[stride * i + 0] = data.position.x;
            vertices[stride * i + 1] = data.position.y;
            vertices[stride * i + 2] = data.position.z;
            if (data.normal != null) {
                vertices[stride * i + 3] = data.normal.x;
                vertices[stride * i + 4] = data.normal.y;
                vertices[stride * i + 5] = data.normal.z;
            }

            if (data.texture != null) {
                vertices[stride * i + 6] = data.texture.x;
                vertices[stride * i + 7] = data.texture.y;
            }
        }
        return vertices;
    }

    protected int[] buildFaces(ModelData obj) {
        final int size = getVertexBufferStride();
        final int tri = 3;
        int[] faces = new int[tri * obj.faces.size()];
        for (int i = 0; i < obj.faces.size(); ++i) {
            assert(obj.faces.get(i).length == tri);
            faces[tri * i] = obj.faces.get(i)[0];
            faces[tri * i + 1] = obj.faces.get(i)[1];
            faces[tri * i + 2] = obj.faces.get(i)[2];
        }
        return faces;
    }

    protected void bufferVertexData(float[] vertices) {
        bufferVertexData(vertices, VBO);
    }

    public static void bufferVertexData(float[] vertices, int VBO) {
        // Needs to be off-heap memory
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, VBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexBuffer, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(vertexBuffer);
    }

    protected void bufferFaceElements(int[] faces) {
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(faces.length);
        indexBuffer.put(faces).flip();
        GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(indexBuffer);
        // Note faces is already a flattened array; its length is the number
        // of vertices to draw
        this.numIndices = faces.length;
    }

    protected void enablePositionNormalTexture() {
        int strideBytes = getVertexBufferStride() * Float.BYTES;
        enablePositionNormalTexture(strideBytes);
    }

    public static void enablePositionNormalTexture(int strideBytes) {
        // First argument depends on the layout value in the vertex shader
        // 0 = position
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, strideBytes, 0);
        GL20.glEnableVertexAttribArray(0);
        // 1 = normal
        GL20.glVertexAttribPointer(1, 3, GL20.GL_FLOAT, true, strideBytes, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        // 2 = texture
        GL20.glVertexAttribPointer(2, 2, GL20.GL_FLOAT, false, strideBytes, 6 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
    }

    // Calling function is responsible for setting the shader
    public void render() {
        GL30.glBindVertexArray(VAO);
        GL20.glDrawElements(GL20.GL_TRIANGLES, this.numIndices, GL20.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void clean() {
        GL30.glBindVertexArray(0);
        GL20.glDeleteBuffers(VBO);
        GL20.glDeleteBuffers(EBO);
        GL30.glDeleteVertexArrays(VAO);
        VAO = 0;
        VBO = 0;
        EBO = 0;
        numIndices = 0;
    }
}
