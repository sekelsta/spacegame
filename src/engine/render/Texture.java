package sekelsta.engine.render;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.Log;

public class Texture {
    private static final String TEXTURE_LOCATION = "/assets/textures/";
    int handle;

    public Texture(String name) {
        BufferedImage image = null;
        try {
            InputStream stream = Texture.class.getResourceAsStream(TEXTURE_LOCATION + name);
            image = ImageIO.read(stream);
        }
        catch (IOException e) {
            Log.error(e.toString());
        }
        if (!isPowerOfTwo(image.getWidth()) || !isPowerOfTwo(image.getHeight())) {
            throw new RuntimeException("Size of texture " + name + " is not a power of two");
        }
        init(image, true);
    }

    public Texture(BufferedImage image, boolean needsMipmaps) {
        init(image, needsMipmaps);
    }

    public Texture(Color color) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        init(image, false);
    }

    private void init(BufferedImage image, boolean needsMipmaps) {
        // TODO: handle non-rgba textures
        handle = GL11.glGenTextures();
        bind();
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        ByteBuffer pixels = convertImage(image);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        // TO_OPTIMIZE: set blend mode that doesn't expect mipmaps for these
        if (true || needsMipmaps) {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        }
    }

    private static ByteBuffer convertImage(BufferedImage image)
    {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = MemoryUtil.memAlloc(4 * image.getWidth() * image.getHeight());

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int pixel = pixels[y * image.getWidth() + x];
                // R, G, B, A
                buffer.put((byte)((pixel >> 16) & 0xff));
                buffer.put((byte)((pixel >> 8) & 0xff));
                buffer.put((byte)((pixel >> 0) & 0xff));
                buffer.put((byte)((pixel >> 24) & 0xff));
            }
        }

        buffer.flip();
        return buffer;
    }

    public void bind() {
        // Activate texture unit before binding texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    public void bindSpecular() {
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    public void bindEmission() {
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && ((n & n - 1) == 0);
    }
}
