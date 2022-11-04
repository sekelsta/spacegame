package sekelsta.engine.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

import sekelsta.engine.Log;

public class ImageUtils {
    public static BufferedImage loadResource(String name) {
        Log.info("Loading image resource: " + name);
        BufferedImage image = null;
        try {
            InputStream stream = ImageUtils.class.getResourceAsStream(name);
            image = ImageIO.read(stream);
        }
        catch (IOException e) {
            Log.error(e.toString());
        }
        return image;
    }

    public static void updateImageBuffer(ByteBuffer buffer, BufferedImage image) {
        assert(buffer.remaining() == 4 * image.getWidth() * image.getHeight());

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        buffer.clear();
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
    }

    public static ByteBuffer bufferedImageToByteBuffer(BufferedImage image)
    {
        // TO_OPTIMIZE: Can use MemoryUtil.memAlloc() instead if I free the memory afterwards
        // See https://stackoverflow.com/questions/65599336/whats-the-difference-between-bufferutils-and-memoryutil-lwjgl
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());
        updateImageBuffer(buffer, image);
        return buffer;
    }
}
