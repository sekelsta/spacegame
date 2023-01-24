package sekelsta.game.render.gui;

import java.awt.Font;
import java.awt.Color;

import sekelsta.engine.render.text.BitmapFont;

public class Fonts {
    private static BitmapFont buttonFont; 
    private static BitmapFont titleFont;
    private static BitmapFont textFont;

    public static Color ERROR_COLOR = new Color(0.8f, 0.1f, 0.1f);

    public static void load() {
        buttonFont = new BitmapFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48), true);
        titleFont = new BitmapFont(new Font(Font.SANS_SERIF, Font.PLAIN, 72), true);
        textFont = new BitmapFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30), true);
    }

    public static BitmapFont getButtonFont() {
        return buttonFont;
    }

    public static BitmapFont getTitleFont() {
        return titleFont;
    }

    public static BitmapFont getTextFont() {
        return textFont;
    }

    public static void render() {
        buttonFont.render();
        titleFont.render();
        textFont.render();
    }

    public static void clean() {
        if (buttonFont != null) {
            buttonFont.clean();
            buttonFont = null;
        }
        if (titleFont != null) {
            titleFont.clean();
            titleFont = null;
        }
        if (textFont != null) {
            textFont.clean();
            textFont = null;
        }
    }
}
