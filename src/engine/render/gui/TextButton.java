package sekelsta.engine.render.gui;

import java.awt.Color;

import sekelsta.engine.render.text.BitmapFont;

public class TextButton {
    private static final Color HIGHLIGHT_COLOR = new Color(0.6f, 0.6f, 0.9f);

    // This class does not "own" the font and is not responsible for rendering or cleanup
    private BitmapFont font;

    private String text;
    private int x, y;
    private int width, height;
    private boolean highlight = false;
    private Runnable onTrigger;

    public TextButton(BitmapFont font, String text, int x, int y, Runnable onTrigger) {
        this.font = font;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = font.getWidth(text);
        this.height = font.getHeight();
        this.onTrigger = onTrigger;
    }

    public TextButton(BitmapFont font, String text, Runnable onTrigger) {
        this(font, text, 0, 0, onTrigger);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean containsPoint(double posX, double posY) {
        return posX >= x && posX < x + width && posY >= y && posY < y + height;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void trigger() {
        onTrigger.run();
    }

    public void blit() {
        blitOffset(0, 0);
    }

    public void blitOffset(int xOffset, int yOffset) {
        if (highlight) {
            font.blit(text, x + xOffset, y + yOffset, 
                HIGHLIGHT_COLOR.getRed() / 255f, HIGHLIGHT_COLOR.getGreen() / 255f, HIGHLIGHT_COLOR.getBlue() / 255f);
        }
        else {
            font.blit(text, x + xOffset, y + yOffset);
        }
    }
}
