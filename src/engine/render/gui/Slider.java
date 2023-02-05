package sekelsta.engine.render.gui;

import sekelsta.engine.render.SpriteBatch;

public class Slider extends GuiElement {
    private static final int BAR_WIDTH = 7;

    private float val;
    private Runnable onValChanged;

    public Slider(float val, Runnable onValueChanged) {
        this.val = val;
        this.onValChanged = onValueChanged;
    }

    public float getValue() {
        return val;
    }

    // Increment
    @Override
    public void right() {
        val = Math.min(1f, val + 0.05f);
        onValChanged.run();
    }

    // Decrement
    @Override
    public void left() {
        val = Math.max(0f, val - 0.05f);
        onValChanged.run();
    }

    @Override
    public void click(double xPos, double yPos) {
        double v = (xPos - this.x - BAR_WIDTH) / usableWidth();
        float vf = Math.max(0f, Math.min(1f, (float)v));
        if (val != vf) {
            val = vf;
            onValChanged.run();
        }
    }

    public void holdLeftMouseButton(double xPos, double yPos) {
        click(xPos, yPos);
    }

    @Override
    public int getWidth() {
        return 256;
    }

    @Override
    public int getHeight() {
        return 26;
    }

    private int usableWidth() {
        return getWidth() - 3 * BAR_WIDTH;
    }

    @Override
    public void blit(SpriteBatch spritebatch, boolean focused) {
        if (focused) {
            spritebatch.blit(x, y, getWidth(), getHeight(), 0, 0, HIGHLIGHT_COLOR);
        }
        else {
            spritebatch.blit(x, y, getWidth(), getHeight(), 0, 0);
        }
        int slide = BAR_WIDTH + (int)(val * usableWidth());
        spritebatch.blit(x + slide, y, BAR_WIDTH, getHeight(), 0, 0);
    }
}
