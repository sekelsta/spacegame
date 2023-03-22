package sekelsta.engine.render;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class TextureArray extends Texture {
    public final int numLayers;

    public TextureArray(String... names) {
        this.numLayers = names.length;
        BufferedImage[] layers = new BufferedImage[names.length];
        for (int i = 0; i < names.length; ++i) {
            layers[i] = ImageUtils.loadResource(TEXTURE_LOCATION + names[i]);
        }
        init(layers);
    }

    public TextureArray(Color color, int numLayers) {
        this.numLayers = numLayers;
        BufferedImage[] layers = new BufferedImage[numLayers];
        BufferedImage image = ImageUtils.makeSinglePixelImage(color);
        for (int i = 0; i < numLayers; ++i) {
            layers[i] = image;
        }
        init(layers);
    }

    private void init(BufferedImage... layers) {
        handle = GL11.glGenTextures();
        bind();
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        assert(layers.length > 0);
        width = layers[0].getWidth();
        height = layers[0].getHeight();
        for (int i = 1; i < layers.length; ++i) {
            assert(layers[i].getWidth() == width);
            assert(layers[i].getHeight() == height);
        }
        this.pixels = ImageUtils.bufferedImagesToByteBuffer(layers);

        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        // TO_OPTIMIZE: call GL42.glTexStorage3D to allocate storage for all mipmap levels at once
        GL12.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL11.GL_RGBA, width, height, layers.length, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        // TODO: mipmapping
    }

    @Override
    public void bind() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, handle);
    }

    @Override
    public void bindEmission() {
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, handle);
    }

    @Override
    public void update(BufferedImage image, boolean needsMipmaps) {
        throw new RuntimeException("Not implemented");
    }
}
