package sekelsta.engine.render.text;

public class Glyph {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public Glyph(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String toString() {
        return "Glyph x=" + x + ", y=" + y + ", width=" + width + ", height=" + height;
    }
}
