package sekelsta.engine.render.gui;

import java.awt.Color;

import sekelsta.engine.render.SpriteBatch;

public abstract class GuiElement {
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f);
    protected static final Color HIGHLIGHT_COLOR = new Color(0.6f, 0.6f, 0.9f);

    protected int x, y;

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public void position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean containsPoint(double posX, double posY) {
        return posX >= x && posX < x + getWidth() && posY >= y && posY < y + getHeight();
    }

    public abstract int getWidth();
    public abstract int getHeight();

    public boolean trigger() {
        return false;
    }

    public void click(double xPos, double yPos) {
        trigger();
    }

    public void inputCharacter(char character) {
        // Do nothing
    }

    public void backspace() {
        // Do nothing
    }

    public void left() {
        // Do nothing
    }

    public void right() {
        // Do nothing
    }

    public abstract void blit(SpriteBatch spritebatch, boolean focused);
}
