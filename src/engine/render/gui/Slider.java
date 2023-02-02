package sekelsta.engine.render.gui;

import sekelsta.engine.render.SpriteBatch;

public class Slider extends GuiElement {
    private static final int BAR_WIDTH = 7;

    private float val;

    public Slider(float val) {
        this.val = val;
    }

    public void increment() {
        val = Math.min(1f, val + 0.05f);
    }

    public void decrement() {
        val = Math.max(0f, val - 0.05f);
    }

    @Override
    public int getWidth() {
        return 256;
    }

    @Override
    public int getHeight() {
        return 26;
    }

    @Override
    public void blit(SpriteBatch spritebatch, boolean focused) {
        if (focused) {
            spritebatch.blit(x, y, getWidth(), getHeight(), 0, 0, HIGHLIGHT_COLOR);
        }
        else {
            spritebatch.blit(x, y, getWidth(), getHeight(), 0, 0);
        }
        int usableWidth = getWidth() - 3 * BAR_WIDTH;
        int slide = BAR_WIDTH + (int)(val * usableWidth);
        spritebatch.blit(x + slide, y, BAR_WIDTH, getHeight(), 0, 0);
    }
}
