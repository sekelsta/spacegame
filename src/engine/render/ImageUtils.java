package sekelsta.engine.render;

import java.awt.Color;
import java.awt.Graphics2D;
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

    public static BufferedImage makeSinglePixelImage(Color color) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        return image;
    }

    public static void updateImageBuffer(ByteBuffer buffer, BufferedImage image) {
        assert(buffer.remaining() == 4 * image.getWidth() * image.getHeight());
        buffer.clear();
        internalUpdateImageBuffer(buffer, image);
        buffer.flip();
    }

    private static void internalUpdateImageBuffer(ByteBuffer buffer, BufferedImage image) {
        assert(buffer.remaining() >= 4 * image.getWidth() * image.getHeight());

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

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
    }

    public static ByteBuffer bufferedImageToByteBuffer(BufferedImage image)
    {
        // TO_OPTIMIZE: Can use MemoryUtil.memAlloc() instead if I free the memory afterwards
        // See https://stackoverflow.com/questions/65599336/whats-the-difference-between-bufferutils-and-memoryutil-lwjgl
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());
        updateImageBuffer(buffer, image);
        return buffer;
    }

    public static ByteBuffer bufferedImagesToByteBuffer(BufferedImage[] images)
    {
        // TO_OPTIMIZE: Can use MemoryUtil.memAlloc() instead if I free the memory afterwards
        // See https://stackoverflow.com/questions/65599336/whats-the-difference-between-bufferutils-and-memoryutil-lwjgl
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * images[0].getWidth() * images[0].getHeight() * images.length);
        for (BufferedImage image : images) {
            internalUpdateImageBuffer(buffer, image);
        }
        buffer.flip();
        return buffer;
    }
}
