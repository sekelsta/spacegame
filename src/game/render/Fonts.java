package sekelsta.game.render;

import java.awt.Font;
import java.awt.Color;

import sekelsta.engine.render.text.BitmapFont;

public class Fonts {
    private static BitmapFont font; 
    private static BitmapFont titleFont;

    public static Color ERROR_COLOR = new Color(0.8f, 0.1f, 0.1f);

    public static void load() {
        font = new BitmapFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48), true);
        titleFont = new BitmapFont(new Font(Font.SANS_SERIF, Font.PLAIN, 72), true);
    }

    public static BitmapFont getButtonFont() {
        return font;
    }

    public static BitmapFont getTitleFont() {
        return titleFont;
    }

    public static void render() {
        font.render();
        titleFont.render();
    }

    public static void clean() {
        if (font != null) {
            font.clean();
            font = null;
        }
        if (titleFont != null) {
            titleFont.clean();
            titleFont = null;
        }
    }
}
