package sekelsta.engine.render;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

import sekelsta.engine.Log;

public class ImageUtils {

    public static ImageRGBA loadResource(String name) {
        if (name.endsWith(".png")) {
            return loadPNG(name);
        }
        Log.info("Loading image resource: " + name);
        BufferedImage image = null;
        try {
            InputStream stream = ImageUtils.class.getResourceAsStream(name);
            image = ImageIO.read(stream);
        }
        catch (IOException e) {
            Log.error(e.toString());
        }
        return new ImageRGBA(image);
    }

    public static ImageRGBA loadPNG(String name) {
        Log.info("Loading image resource: " + name);
        ImageRGBA image = null;
        try {
            InputStream stream = ImageUtils.class.getResourceAsStream(name);
            PNGDecoder png = new PNGDecoder(stream);
            // TO_OPTIMIZE: Can use MemoryUtil.memAlloc() instead if I free the memory afterwards
            // https://stackoverflow.com/questions/65599336/whats-the-difference-between-bufferutils-and-memoryutil-lwjgl
            ByteBuffer buffer = ByteBuffer.allocateDirect(4 * png.getWidth() * png.getHeight());
            // TO_OPTIMIZE: Handle grayscale using
            // Format format = png.decideTextureFormat();
            png.decode(buffer, png.getWidth() * 4, Format.RGBA);
            buffer.flip();
            image = new ImageRGBA(png.getWidth(), png.getHeight(), buffer);
        }
        catch (IOException e) {
            Log.error(e.toString());
        }
        return image;
    }

    public static ImageRGBA makeSinglePixelImage(Color color) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        buffer.put((byte)color.getRed());
        buffer.put((byte)color.getGreen());
        buffer.put((byte)color.getBlue());
        buffer.put((byte)color.getAlpha());
        buffer.flip();
        return new ImageRGBA(1, 1, buffer);
    }
}
