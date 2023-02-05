package sekelsta.engine.render.gui;

import java.util.List;
import java.util.function.Consumer;

import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.text.BitmapFont;

public class TextChoice extends GuiElement {
    private static final int INVALID = -1;

    private BitmapFont font;

    private List<String> choices;
    private int chosen;
    private int selected;

    private int spacing;

    private Consumer<Integer> onItemChosen;

    public TextChoice(BitmapFont font, List<String> choices, int chosen, Consumer<Integer> onItemChosen) {
        assert(choices.size() > 0);
        this.choices = choices;
        this.font = font;
        spacing = font.getHeight() * 2;
        this.chosen = chosen;
        selected = chosen;
        this.onItemChosen = onItemChosen;
    }

    @Override
    public int getWidth() {
        int textWidth = 0;
        for (String text : choices) {
            textWidth += font.getWidth(text);
        }
        return textWidth + (choices.size() - 1) * spacing;
    }

    @Override
    public int getHeight() {
        return font.getHeight();
    }

    @Override
    public boolean trigger() {
        chosen = selected;
        onItemChosen.accept(chosen);
        return true;
    }

    public void hover(double xPos, double yPos) {
        int xLoc = this.x;
        for (int i = 0; i < choices.size(); ++i) {
            if (xPos < xLoc) {
                return;
            }
            xLoc += font.getWidth(choices.get(i));
            if (xPos < xLoc) {
                selected = i;
                return;
            }
            xLoc += spacing;
        }
    }

    @Override
    public void click(double xPos, double yPos) {
        hover(xPos, yPos);
        trigger();
    }

    @Override
    public void left() {
        if (selected > 0) {
            selected -= 1;
        }
    }

    @Override
    public void right() {
        if (selected + 1 < choices.size()) {
            selected += 1;
        }
    }

    @Override
    public void blit(SpriteBatch spritebatch, boolean focused) {
        int xPos = x;
        for (int i = 0; i < choices.size(); ++i) {
            String text = choices.get(i);
            if (focused && i == selected) {
                if (i == chosen) {
                    font.blitUnderlined(text, xPos, y, GuiElement.HIGHLIGHT_COLOR);
                }
                else {
                    font.blit(text, xPos, y, GuiElement.HIGHLIGHT_COLOR);
                }
            }
            else {
                if (i == chosen) {
                    font.blitUnderlined(text, xPos, y);
                }
                else {
                    font.blit(text, xPos, y);
                }
            }

            xPos += font.getWidth(text);
            xPos += spacing;
        }
    }
}
