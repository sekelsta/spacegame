package sekelsta.engine.render;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import shadowfox.math.Matrix3f;
import shadowfox.math.Matrix4f;
import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;

public class ShaderProgram {
    private int handle;

    public ShaderProgram(String vertexSource, String fragmentSource) {
        handle = GL20.glCreateProgram();
        int vertexShader = loadShader(vertexSource, GL20.GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);
        GL20.glAttachShader(handle, vertexShader);
        GL20.glAttachShader(handle, fragmentShader);
        GL20.glLinkProgram(handle);

        boolean linked = GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS) != 0;
        if (!linked) {
            throw new RuntimeException("Failed to link shaders\n"
                + GL20.glGetProgramInfoLog(handle)
            );
        }

        GL20.glDetachShader(handle, vertexShader);
        GL20.glDetachShader(handle, fragmentShader);
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    public static ShaderProgram load(String vertexResource, String fragmentResource) {
        return new ShaderProgram(loadResource(vertexResource), loadResource(fragmentResource));
    }

    protected static String loadResource(String name) {
        try {
            return new String(ShaderProgram.class.getResourceAsStream(name).readAllBytes());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int loadShader(String source, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        boolean compiled = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != 0;
        if (!compiled) {
            throw new RuntimeException("Failed to compile shader\n" 
                + GL20.glGetShaderInfoLog(shader)
            );
        }

        return shader;
    }

    public void use() {
        GL20.glUseProgram(handle);
    }

    public void delete() {
        GL20.glDeleteProgram(handle);
    }

    private int getUniformLocation(String uniform) {
        int location = GL20.glGetUniformLocation(handle, uniform);
        if (location == -1) {
            throw new RuntimeException("Shader uniform not found:" + uniform);
        }
        return location;
    }

    public void setInt(String uniform, int val) {
        int location = getUniformLocation(uniform);
        GL20.glUniform1i(location, val);
    }

    public void setFloat(String uniform, float val) {
        int location = getUniformLocation(uniform);
        GL20.glUniform1f(location, val);
    }

    public void setUniform(String uniform, Vector2f v) {
        int location = getUniformLocation(uniform);
        GL20.glUniform2f(location, v.x, v.y);
    }

    public void setUniform(String uniform, Vector3f v) {
        int location = getUniformLocation(uniform);
        GL20.glUniform3f(location, v.x, v.y, v.z);
    }

    public void setUniform(String uniform, float f1, float f2, float f3, float f4) {
        int location = getUniformLocation(uniform);
        GL20.glUniform4f(location, f1, f2, f3, f4);
    }

    public void setUniform(String uniform, Matrix3f matrix) {
        int location = getUniformLocation(uniform);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * 3);
            matrix.storeColumnMajor(buffer);
            buffer.flip();
            GL20.glUniformMatrix3fv(location, false, buffer);
        }
    }

    public void setUniform(String uniform, Matrix4f matrix) {
        int location = getUniformLocation(uniform);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 4);
            matrix.storeColumnMajor(buffer);
            buffer.flip();
            GL20.glUniformMatrix4fv(location, false, buffer);
        }
    }
}
