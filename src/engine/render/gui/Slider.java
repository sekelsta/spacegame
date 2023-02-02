package sekelsta.engine.render.gui;

import sekelsta.engine.render.SpriteBatch;

public class Slider extends GuiElement {
    private static final int BAR_WIDTH = 7;

    private float val;

    public Slider(float val) {
        this.val = val;
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
        int slide = BAR_WIDTH + (int)(val * (getWidth() - 3 * BAR_WIDTH));
        spritebatch.blit(x + slide, y, BAR_WIDTH, getHeight(), 0, 0);
    }
}
