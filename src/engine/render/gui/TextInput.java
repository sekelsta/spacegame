package sekelsta.engine.render.gui;

import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.text.BitmapFont;

public class TextInput extends TextElement {
    private String hint;

    public TextInput(BitmapFont font, String defaultText, String hint) {
        super(font, defaultText);
        this.hint = hint;
    }

    public String getEnteredText() {
        return text;
    }

    @Override
    public int getWidth() {
        return Math.max(font.getWidth(text), font.getWidth(hint));
    }

    @Override
    public void inputCharacter(char character) {
        text = text + character;
    }

    @Override
    public void backspace() {
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
    }

    @Override
    public void blit(SpriteBatch spritebatch, boolean focused) {
        int textWidth = font.getWidth(text);
        int hintWidth = font.getWidth(hint);
        int xOffset = 0;
        if (hintWidth > textWidth && (focused || text != "")) {
            xOffset = (hintWidth - textWidth) / 2;
        }
        if (focused) {
            font.blit(text, x + xOffset, y);
            font.blitCursor(x + xOffset + font.getWidth(text), y, 1f, 1f, 1f);
        }
        else if (text != "") {
            font.blit(text, x + xOffset, y);
        }
        else {
            font.blit(hint, x + xOffset, y,
                GuiElement.GRAY.getRed() / 255f, GuiElement.GRAY.getGreen() / 255f, GuiElement.GRAY.getBlue() / 255f);
        }
    }
}
