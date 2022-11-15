package sekelsta.game.render;

import java.util.*;

import sekelsta.engine.render.gui.TextButton;

public class TextButtonList {
    private static final int INVALID = -1;
    private List<TextButton> buttons = new ArrayList<>();
    private int selected = INVALID;

    private int x, y;

    public int getWidth() {
        int width = 0;
        for (TextButton button : buttons) {
            width = Math.max(width, button.getWidth());
        }
        return width;
    }

    public int getHeight() {
        TextButton lastButton = buttons.get(buttons.size() - 1);
        return lastButton.getY() + lastButton.getHeight();
    }

    public void position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void add(TextButton button) {
        buttons.add(button);
        recalculatePositions();
    }

    private void recalculatePositions() {
        int width = this.getWidth();
        int spacing = buttons.size() == 0 ? 0 : buttons.get(0).getHeight() / 2;
        int yPos = 0;
        for (TextButton button : buttons) {
            int xPos = (width - button.getWidth()) / 2;
            button.position(xPos, yPos);
            yPos += spacing + button.getHeight();
        }
    }

    protected void selectButton(int index) {
        if (selected != INVALID) {
            buttons.get(selected).setHighlight(false);
        }
        selected = index;
        if (index != INVALID) {
            buttons.get(selected).setHighlight(true);
        }
    }

    public void positionPointer(double xPos, double yPos) {
        selectButton(INVALID);
        for (int i = 0; i < buttons.size(); ++i) {
            TextButton button = buttons.get(i);
            if (button.containsPoint(xPos - x, yPos - y)) {
                selectButton(i);
            }
        }
    }

    public boolean trigger() {
        if (selected == INVALID) {
            return false;
        }
        buttons.get(selected).trigger();
        return true;
    }

    public boolean up() {
        if (selected > 0) {
            selectButton(selected - 1);
        }
        return buttons.size() > 0;
    }

    public boolean down() {
        if (selected + 1 < buttons.size()) {
            selectButton(selected + 1);
        }
        return buttons.size() > 0;
    }

    public boolean top() {
        if (buttons.size() > 0) {
            selectButton(0);
            return true;
        }
        return false;
    }

    public boolean bottom() {
        if (buttons.size() > 0) {
            selectButton(buttons.size() - 1);
            return true;
        }
        return false;
    }

    public void blit() {
        for (TextButton button : buttons) {
            button.blitOffset(x, y);
        }
    }
}
