package sekelsta.engine.render.gui;

import java.awt.Color;

public abstract class GuiElement {
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f);


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

    public abstract void blit(boolean focused);
}
