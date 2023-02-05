package sekelsta.engine.render.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.Texture;

public class BitmapFont {
    // ASCII 0-31 are control characters
    private static final int STARTING_CHAR = 32;
    private static final int CHAR_MAX = 256;

    private Glyph[] glyphs;
    private Glyph cursor;
    private SpriteBatch spritebatch = new SpriteBatch();

    public BitmapFont(Font font, boolean antialias) {
        FontMetrics metrics = getMetrics(font, antialias);
        // Arrange the glyphs into a texture atlas
        // TO_OPTIMIZE: FontMetrics.charWidth() does not technically return the width of the char, but rather the
        // advance. So the texture atlas could be made smaller by packing to the actual width.
        glyphs = new Glyph[CHAR_MAX - STARTING_CHAR];
        ArrayList<Integer> lengths = new ArrayList<>();
        lengths.add(0);
        int charHeight = metrics.getHeight();
        for (int i = STARTING_CHAR; i < CHAR_MAX; ++i) {
            if (!Character.isValidCodePoint(i)) {
                continue;
            }
            if (!font.canDisplay(i)) {
                continue;
            }
            int charWidth = metrics.charWidth((char)i);
            if (charWidth == 0) {
                continue;
            }
            glyphs[i - STARTING_CHAR] = makeGlyph(charWidth, charHeight, lengths);
        }
        int cursorWidth = (int)Math.ceil(font.getSize2D() / 18f);
        cursor = makeGlyph(cursorWidth, charHeight, lengths);

        int imageWidth = 0;
        for (int x : lengths) {
            imageWidth = Math.max(x, imageWidth);
        }
        int imageHeight = lengths.size() * charHeight;
        // TO_OPTIMIZE: investigate whether ARGB is needed since we're just drawing alpha and white
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
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
            Glyph glyph = glyphs[index];
            if (glyph == null) {
                continue;
            }

            // Draw the glyph onto a separate texture, then copy it over. If we draw directly on the atlas texture,
            // Java may do weird things with the spacing that make glyphs overlap in a bad way
            BufferedImage charImage = new BufferedImage(glyph.width, glyph.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D cg = charImage.createGraphics();
            if (antialias) {
                cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            cg.setFont(font);
            cg.setColor(Color.WHITE);
            cg.drawString(String.valueOf((char)(i)), 0, metrics.getAscent());
            cg.dispose();

            g.drawImage(charImage, null, glyph.x, glyph.y);
        }
        g.setColor(Color.WHITE);
        g.fillRect(cursor.x, cursor.y, cursor.width, cursor.height);
        g.dispose();

        spritebatch.setTexture(new Texture(image, false));
    }

    private Glyph makeGlyph(int charWidth, int charHeight, ArrayList<Integer> lengths) {
        int sideLength = charHeight * lengths.size();
        int line = 0;
        while (lengths.get(line) + charWidth > sideLength) {
            line += 1;
            if (line >= lengths.size()) {
                lengths.add(0);
                sideLength += charHeight;
            }
        }
        lengths.set(line, lengths.get(line) + charWidth);
        return new Glyph(lengths.get(line) - charWidth, line * charHeight, charWidth, charHeight);
    }

    public int getWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += glyphs[c - STARTING_CHAR].width;
        }
        return width;
    }

    public int getHeight() {
        return glyphs[0].height;
    }

    public void blit(char c, int x, int y, float r, float g, float b) {
        Glyph glyph = glyphs[c - STARTING_CHAR];
        spritebatch.blit(x, y, glyph.width, glyph.height, glyph.x, glyph.y, r, g, b);
    }

    public void blit(String s, int x, int y) {
        blit(s, x, y, 1f, 1f, 1f);
    }

    public void blit(String s, int x, int y, Color color) {
        blit(s, x, y, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
    }

    // TODO #31: Handle newlines and such
    public int blit(String s, int x, int y, float r, float g, float b) {
        int w = 0;
        for (char c : s.toCharArray()) {
            blit(c, x + w, y, r, g, b);
            w += glyphs[c - STARTING_CHAR].width;
        }
        return w;
    }


    public void blitUnderlined(String s, int x, int y) {
        blitUnderlined(s, x, y, 1f, 1f, 1f);
    }

    public void blitUnderlined(String s, int x, int y, Color color) {
        blitUnderlined(s, x, y, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
    }

    public void blitUnderlined(String s, int x, int y, float r, float g, float b) {
        int w = blit(s, x, y, r, g, b);
        Glyph underscore = glyphs['_' - STARTING_CHAR];
        // Adjust texture position to avoid blurring at the edges
        spritebatch.blitStretched(x, y, w, underscore.height, 
                underscore.x + 1, underscore.y + 1, underscore.width - 2, underscore.height - 2,
                r, g, b);
    }

    public void blitCursor(int x, int y, float r, float g, float b) {
        spritebatch.blit(x, y, cursor.width, cursor.height, cursor.x, cursor.y, r, g, b);
    }

    public void render() {
        spritebatch.render();
    }

    public void clean() {
        spritebatch.clean();
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
