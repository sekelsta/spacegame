package sekelsta.engine.render;

import java.awt.Color;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;


public class Texture {
    protected static final String TEXTURE_LOCATION = "/assets/textures/";
    protected int handle;
    protected int width;
    protected int height;

    public Texture(String name) {
        ImageRGBA image = ImageUtils.loadResource(TEXTURE_LOCATION + name);
        if (!isPowerOfTwo(image.getWidth()) || !isPowerOfTwo(image.getHeight())) {
            throw new RuntimeException("Size of texture " + name + " is not a power of two");
        }
        init(image, true);
    }

    public Texture(Color color) {
        ImageRGBA image = ImageUtils.makeSinglePixelImage(color);
        init(image, false);
    }

    public Texture(ImageRGBA image, boolean needsMipmaps) {
        init(image, needsMipmaps);
    }

    protected Texture() {}

    private void init(ImageRGBA image, boolean needsMipmaps) {
        this.width = image.width;
        this.height = image.height;
        handle = GL11.glGenTextures();
        bind();
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        if (!needsMipmaps) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.pixels);

        if (needsMipmaps) {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void bind() {
        // Activate texture unit before binding texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    public void bindEmission() {
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    protected boolean isPowerOfTwo(int n) {
        return n > 0 && ((n & n - 1) == 0);
    }
}
