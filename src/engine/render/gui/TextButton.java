package sekelsta.engine.render.gui;

import java.awt.Color;

import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.text.BitmapFont;

public class TextButton extends TextElement {
    private Runnable onTrigger;

    public TextButton(BitmapFont font, String text, int x, int y, Runnable onTrigger) {
        super(font, text, Color.WHITE, x, y);
        this.onTrigger = onTrigger;
    }

    public TextButton(BitmapFont font, String text, Runnable onTrigger) {
        this(font, text, 0, 0, onTrigger);
    }

    @Override
    public boolean trigger() {
        onTrigger.run();
        return true;
    }

    @Override
    public void blit(SpriteBatch spritebatch, boolean focused) {
        if (focused) {
            font.blit(text, x, y, 
                HIGHLIGHT_COLOR.getRed() / 255f, HIGHLIGHT_COLOR.getGreen() / 255f, HIGHLIGHT_COLOR.getBlue() / 255f);
        }
        else {
            font.blit(text, x, y, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
        }
    }
}
