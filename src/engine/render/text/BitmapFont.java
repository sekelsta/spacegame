package sekelsta.engine.render.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import sekelsta.engine.render.Texture;

public class BitmapFont {
    // ASCII 0-31 are control characters
    private static final int STARTING_CHAR = 32;
    private static final int CHAR_MAX = 256;

    private Glyph[] glyphs;
    private Texture texture;

    public BitmapFont() {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        boolean antialias = true;
        FontMetrics metrics = getMetrics(font, antialias);
        // Set up glyph info
        glyphs = new Glyph[CHAR_MAX - STARTING_CHAR];
        int x = 0;
        int charHeight = metrics.getHeight();
        // TO_OPTIMIZE: try breaking this into multiple lines and see if it helps with performance
        // (plain monospaced 16p font is getting me a 1910 x 19 pixel texture)
        for (int i = STARTING_CHAR; i < CHAR_MAX; ++i) {
            if (!Character.isValidCodePoint(i)) {
                continue;
            }
            if (!font.canDisplay(i)) {
                continue;
            }
            int charWidth = metrics.charWidth((char)i);
            if (charWidth != 0) {
                glyphs[i - STARTING_CHAR] = new Glyph(x, 0, charWidth, charHeight);
                x += charWidth;
            }
        }

        // TO_OPTIMIZE: investigate whether ARGB is needed since we're just drawing alpha and white
        BufferedImage image = new BufferedImage(x, charHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        metrics = g.getFontMetrics();
        // Render white so we can easily recolor
        g.setColor(Color.WHITE);
        // Draw each glyph onto the texture
        for (int i = STARTING_CHAR; i < CHAR_MAX; ++i) {
            int index = i - STARTING_CHAR;
            if (glyphs[index] == null) {
                continue;
            }
            g.drawString(String.valueOf((char)(i)), glyphs[index].x, metrics.getAscent());
        }
        g.dispose();

        this.texture = new Texture(image, false);
    }

    private FontMetrics getMetrics(Font font, boolean antialias) {
        // Create temporary image to get font metrics
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tempImage.createGraphics();
        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();
        return metrics;
    }

    // Call from the constructor to examine the texture atlas
    private void debugSaveImage(BufferedImage image) {
        try {
            java.io.File output = new java.io.File("fontmap.png");
            javax.imageio.ImageIO.write(image, "png", output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
